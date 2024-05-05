create database SearchEngine;
use SearchEngine;
create table Words(
word varchar(200) not null,
idf float, 
primary key(word)
);
create table Positions(
word varchar(200) not null,
document varchar(300) not null,
position int not null, 
primary key(word,Document,position)
);
create table Documents(
word varchar(200) not null,
document varchar(300) not null,
tf int,
type varchar(20), 
primary key(word,document)
);
create table URLs(
document varchar(300) not null,
indexed bool Default false, 
primary key(document)
);


