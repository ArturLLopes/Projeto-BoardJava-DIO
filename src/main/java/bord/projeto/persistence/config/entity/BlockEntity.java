package bord.projeto.persistence.config.entity;

import lombok.Data;

import java.time.OffsetDateTime;

@Data
public class BlockEntity {

    private  Long id;
    private OffsetDateTime blockAt;
    private String blockReason;
    private OffsetDateTime unblockAt;
    private String unblockReason;

}
