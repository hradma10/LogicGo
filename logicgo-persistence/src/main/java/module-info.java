module cz.logicgo.persistence {
    requires cz.logicgo.core;
    requires jakarta.persistence;
    requires org.hibernate.orm.core;
    requires java.sql;
    requires com.fasterxml.jackson.databind;
    requires bcrypt;

    exports cz.logicgo.persistence.dao;
    exports cz.logicgo.persistence.services;
    exports cz.logicgo.persistence.jpa;
    exports cz.logicgo.persistence.filter;
    exports cz.logicgo.persistence.exceptions.database.user;
}
