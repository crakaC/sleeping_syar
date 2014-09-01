create table users
(
	UserID text UNIQUE NOT NULL,
	ScreenName	text,
	IconURL text,
	IsCurrent integer default -1,
	Token text,
	TokenSecret text
)