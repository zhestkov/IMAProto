insert into User(login, password) values ('admin', 'pass');

-- create mailboxes:
insert into Mailbox(name, User_id, parentId) values("INBOX", 1, 0);
insert into Mailbox(name, User_id, parentId) values("test box", 1, 0);

-- INBOX msg#1:
insert into Message(User_id, Mailbox_id, flagSeen, flagAnswered, flagDeleted, flagDraft,
flagRecent, flagFlagged)
values (1, 1, 0, 0, 0, 0, 1, 0);

-- INBOX msg#2:
insert into Message(User_id, Mailbox_id, flagSeen, flagAnswered, flagDeleted, flagDraft,
flagRecent, flagFlagged)
values (1, 1, 0, 0, 0, 0, 1, 0);

-- first unseen message id
select * from Message 
where flagSeen = 0 and Mailbox_id in (select id from Mailbox where Mailbox.name = "INBOX"
	and Mailbox.User_id = 1) and Message.id = (select max(Message.id) from Message);

-- count exist messages:
select count(1) as amount from Message where Mailbox_id =
	(select id from Mailbox where Mailbox.name = "INBOX" and Mailbox.User_id = 1);

-- count recent messages:
select count(1) as amount from Message where flagRecent = 1 and Mailbox_id =
	(select id from Mailbox where Mailbox.name = "INBOX" and Mailbox.User_id = 1);

