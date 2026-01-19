package ru.itmo.se.is.cw.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Entity
@Table(name = "client_application")
public class ClientApplicationEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @ManyToOne(optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "client_id", nullable = false)
    private ClientEntity client;

    @Column(name = "description", nullable = false, length = Integer.MAX_VALUE)
    private String description;

    @Column(name = "created_at", nullable = false)
    @CreationTimestamp
    private ZonedDateTime createdAt;

    @ManyToOne
    @OnDelete(action = OnDeleteAction.SET_NULL)
    @JoinColumn(name = "template_product_design_id")
    private ProductDesignEntity templateProductDesign;

    @Column(name = "amount", nullable = false)
    private Short amount;

    @OneToMany(mappedBy = "clientApplication", cascade = CascadeType.ALL, orphanRemoval = true)
    List<ClientApplicationAttachmentEntity> attachments = new ArrayList<>();

    public void addAttachment(ClientApplicationAttachmentEntity attachment) {
        attachments.add(attachment);
        attachment.setClientApplication(this);
    }
}