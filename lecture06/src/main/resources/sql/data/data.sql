--- Test insert
insert into chat.user (login)
values ('tom');
insert into chat.user (login)
values ('bob');
insert into chat.message ("user",time,value)
values (2,now(),'hello');
delete from chat.user where login=bob;
--- Test constraints
insert into chat.user (login)
values ('admin');


insert into chat.message ("user", time, value)
values (currval('chat.user_id_seq'), now(), 'my super user message');

delete from chat.user where login = 'admin';

--- select test
select *
from chat.message
where time > '2017-03-25';

