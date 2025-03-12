package bord.projeto.dto;

import java.time.OffsetDateTime;



public record CardDetailsDTO(Long id,
                             String title,
                             String description,
                             boolean block,
                             OffsetDateTime blockAt,
                             String blockReason,
                             int blocksAmount,
                             Long columnId,
                             String columnName
) {
}