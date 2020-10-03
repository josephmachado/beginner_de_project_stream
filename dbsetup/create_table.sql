CREATE TABLE server_log (
	eventId varchar(50),
	userId int,
	eventType varchar(15),
	locationCountry varchar(25),
	eventTimeStamp varchar(25),
	PRIMARY KEY (eventId)
);