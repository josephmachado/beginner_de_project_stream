package com.startdataengineering

import org.apache.flink.api.common.state.{ValueState, ValueStateDescriptor}
import org.apache.flink.api.common.typeinfo.Types
import org.apache.flink.configuration.Configuration
import org.apache.flink.streaming.api.functions.KeyedProcessFunction
import org.apache.flink.util.Collector

import com.startdataengineering.model.ServerLog

class FraudDetection extends KeyedProcessFunction[String, String, String]{

  private var loginState: ValueState[java.lang.Boolean] = _
  private var prevLoginCountry: ValueState[java.lang.String] = _
  private var timerState: ValueState[java.lang.Long] = _

  @throws[Exception]
  override def open(parameters: Configuration): Unit = {
    val loginDescriptor = new ValueStateDescriptor("login-flag", Types.BOOLEAN)
    loginState = getRuntimeContext.getState(loginDescriptor)

    val prevCountryDescriptor = new ValueStateDescriptor("prev-country", Types.STRING)
    prevLoginCountry = getRuntimeContext.getState(prevCountryDescriptor)

    val timerStateDescriptor = new ValueStateDescriptor("timer-state", Types.LONG)
    timerState = getRuntimeContext.getState(timerStateDescriptor)
  }

  @throws[Exception]
  override def processElement(
                               value: String,
                               ctx: KeyedProcessFunction[String, String, String]#Context,
                               out: Collector[String]): Unit = {
    val logEvent: ServerLog = ServerLog.fromString(value)

    val isLoggedIn = loginState.value
    val prevCountry = prevLoginCountry.value

    if ((isLoggedIn != null) && (prevCountry != null)){
      if ((isLoggedIn == true) && (logEvent.eventType == "login")) {
        // if account already logged in and tries another login from another country, send alert event
        if (prevCountry != logEvent.locationCountry) {
          val alert: String = f"Alert eventID: ${logEvent.eventId}%s, " +
            f"violatingAccountId: ${logEvent.accountId}%d, prevCountry: ${prevCountry}%s, " +
            f"currentCountry: ${logEvent.locationCountry}%s"
          out.collect(alert)
        }
      }
    }
    else if (logEvent.eventType == "login"){
      // set login and set prev login country
      loginState.update(true)
      prevLoginCountry.update(logEvent.locationCountry)

      // as soon as the account user logs in, we set a timer for 5 min for this account ID
      // 5 * 60 * 1000L -> 5 min, time is expected in Long format
      val timer = logEvent.eventTimeStamp + (5 * 60 * 1000L)
      ctx.timerService.registerProcessingTimeTimer(timer)
      timerState.update(timer)
    }
    else if (logEvent.eventType == "log-out") {
      // reset prev login and country
      loginState.clear()
      prevLoginCountry.clear()

      // remove timer, if it is set
      val timer = timerState.value()
      if (timer != null){
        ctx.timerService.deleteProcessingTimeTimer(timer)
      }
      timerState.clear()
    }
  }

  @throws[Exception]
  override def onTimer(timestamp: Long,
                       ctx: KeyedProcessFunction[String, String, String]#OnTimerContext,
                       out: Collector[String]): Unit = {
    // when the timer runs out, this method gets called, we clear the loginState and prevLoginCountry
    timerState.clear()
    loginState.clear()
    prevLoginCountry.clear()
  }
}
