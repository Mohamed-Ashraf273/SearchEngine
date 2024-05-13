create database SearchEngine;
use SearchEngine;
create table Positions(
word varchar(200) not null,
document varchar(300) not null,
position int not null, 
primary key(word,Document,position)
);
create table Documents(
word varchar(200) not null,
document varchar(300) not null,
tf_idf double,
type varchar(20),
primary key(word,document)
);
create table URLs(
document varchar(300) not null,
indexed bool Default false, 
primary key(document)
);
create table queue(
id int AUTO_INCREMENT,
document varchar(1000) not null,
primary key(id)
);
create table crawlset(
document varchar(600) not null,
primary key(document)
);



