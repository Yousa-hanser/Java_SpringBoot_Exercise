package com.cy.store.entity;

import java.io.Serializable;
import java.util.Date;

/* 作为实体类的基类 */
public class BaseEntity implements Serializable {
    private String created_user;
    private Date created_time;
    private String modified_user;
    private Date modified_time;
}
