CREATE TABLE IF NOT EXISTS adyen_account(
  `id` int(11) NOT NULL,
  `transaction_id` int(11) NOT NULL,
  `email` varchar(255) DEFAULT NULL,
  `first_name` varchar(255) DEFAULT NULL,
  `last_name` varchar(255) DEFAULT NULL,
  `login` varchar(255) DEFAULT NULL,
  `password` varchar(255) DEFAULT NULL,
  `active` BIT,
  `user_group` VARCHAR(255) DEFAULT NULL,
  `market` VARCHAR(15) DEFAULT NULL,
  `full_name` varchar(255) DEFAULT NULL,
  `status` varchar(255) DEFAULT NULL,
  `error` VARCHAR(255) DEFAULT NULL,
  `file_name` varchar(255) DEFAULT NULL,
  `psp_reference` varchar(255) DEFAULT NULL,
  `environment` VARCHAR(255) DEFAULT NULL,
  `create_date` timestamp DEFAULT NULL,
  `last_update_date` timestamp DEFAULT NULL,

  PRIMARY KEY (`id`),
  UNIQUE KEY `UK_adyen_account_email` (`email`)
  ) ENGINE=MyISAM DEFAULT CHARSET=utf8;

CREATE TABLE IF NOT EXISTS `temp_account` (
  `login` varchar(255) NOT NULL,
  `email` varchar(255) DEFAULT NULL,
  `first_name` varchar(255) DEFAULT NULL,
  `last_name` varchar(255) DEFAULT NULL,
  
  `last_update_date` timestamp DEFAULT NULL,

    PRIMARY KEY (`login`)
  ) ENGINE=MyISAM DEFAULT CHARSET=utf8;
 