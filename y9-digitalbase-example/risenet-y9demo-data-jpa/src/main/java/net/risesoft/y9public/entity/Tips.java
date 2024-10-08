package net.risesoft.y9public.entity;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.hibernate.annotations.Comment;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.GenericGenerator;
import org.springframework.format.annotation.DateTimeFormat;

import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "RISESOFT_DEMO_TIPS")
@DynamicUpdate
@org.hibernate.annotations.Table(comment = "提示详情", appliesTo = "RISESOFT_DEMO_TIPS")
@NoArgsConstructor
@Data
public class Tips implements Serializable {
    private static final long serialVersionUID = 5858697659406924473L;

    @Id
    @Column(name = "ID", length = 50, nullable = false)
    @GeneratedValue(generator = "uuid")
    @GenericGenerator(name = "uuid", strategy = "assigned")
    @Comment(value = "主键")
    private String id;

    @Column(name = "USER_ID", length = 50)
    @Comment(value = "人员主键")
    private String userId;

    @Column(name = "MESSAGE", length = 1000)
    @Comment(value = "提示详情")
    private String message;

    @Column(name = "LINK", length = 500)
    @Comment(value = "链接地址")
    private String link;

    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "CREATE_TIME")
    @Comment(value = "创建时间")
    private Date createTime;

}
