CREATE TABLE BUSINESS(
BUSINESS_ID VARCHAR(100) PRIMARY KEY,
FULL_ADDRESS VARCHAR(500) NOT NULL,
OPEN VARCHAR(5),
CITY VARCHAR(25)NOT NULL,
REVIEW_COUNT NUMBER(5),
BUSINESS_NAME VARCHAR(300)NOT NULL,
NEIGHBORHOODS LONG,
LONGITUDE NUMBER NOT NULL,
STATE VARCHAR(50)NOT NULL,
STARS NUMBER(5),
LATITUDE NUMBER NOT NULL,
TYPE VARCHAR(25));

CREATE TABLE Business_To_Category (
Business_Id VARCHAR(100),
Category VARCHAR(100),
PRIMARY KEY(Business_Id, Category)
);


CREATE TABLE Business_To_Sub_Category (
Business_Id VARCHAR(100),
Sub_Category VARCHAR(100),
PRIMARY KEY(Business_Id, Sub_Category)
);


CREATE TABLE BUSINESS_TO_ATTRIBUTE (
BUSINESS_ID VARCHAR(100),
ATTRIBUTE_NAME varchar(100),
FOREIGN KEY(BUSINESS_ID)REFERENCES BUSINESS(BUSINESS_ID)
);


CREATE TABLE YELP_USERS(
YELPING_SINCE DATE,
REVIEW_COUNT NUMBER,
USER_NAME VARCHAR(50),
USER_ID VARCHAR(50)PRIMARY KEY,
FANS NUMBER,
AVERAGE_STARS NUMBER,
USER_TYPE VARCHAR(25),
ELITE LONG,
FRIENDS_COUNT INTEGER
);


CREATE TABLE VOTES_USER(
USER_ID VARCHAR(50),
FUNNY NUMBER,
USEFUL NUMBER,
COOL NUMBER,
COUNT_OF_VOTES NUMBER,
FOREIGN KEY(USER_ID)REFERENCES YELP_USERS(USER_ID)ON
DELETE CASCADE
)
;

CREATE TABLE REVIEWS(
USER_ID VARCHAR(50),
REVIEW_ID VARCHAR(50) PRIMARY KEY,
STARS NUMBER,
DATEE DATE,
TEXT LONG,
TYPE VARCHAR(20),
BUSINESS_ID VARCHAR(50),
FOREIGN KEY(USER_ID)REFERENCES YELP_USERS(USER_ID),
FOREIGN KEY(BUSINESS_ID)REFERENCES BUSINESS(BUSINESS_ID)
);
CREATE TABLE VOTES_REVIEW(
REVIEW_ID VARCHAR(50),
USER_ID VARCHAR(50),
BUSINESS_ID VARCHAR(50),
FUNNY NUMBER,
USEFUL NUMBER,
COOL NUMBER,
FOREIGN KEY(BUSINESS_ID)REFERENCES  BUSINESS(BUSINESS_ID)ON
DELETE CASCADE,
FOREIGN KEY(USER_ID)REFERENCES  YELP_USERS(USER_ID)ON
DELETE CASCADE,
FOREIGN KEY(REVIEW_ID)REFERENCES REVIEWS(REVIEW_ID)ON
DELETE CASCADE
)
;

create index Bus_Cat on Business_To_Category(Category);
CREATE INDEX REVIEW_set ON REVIEWS(BUSINESS_ID, USER_ID);
CREATE INDEX Bus_SC ON Business_To_Sub_Category(Sub_Category);
create index Bus_Att on BUSINESS_TO_ATTRIBUTE(BUSINESS_ID, ATTRIBUTE_NAME);

CREATE INDEX User_Set ON YELP_USERS(USER_NAME);
CREATE INDEX User_Member ON YELP_USERS(yelping_since);
CREATE INDEX User_Review ON YELP_USERS(review_count);
CREATE INDEX User_STAR ON YELP_USERS(AVERAGE_STARS);
create index User_Friends on YELP_USERS(FRIENDS_COUNT);

CREATE INDEX INDEX_REVIEW_DATE ON REVIEWS(DATEE);
CREATE INDEX INDEX_REVIEW_STAR ON REVIEWS(stars);