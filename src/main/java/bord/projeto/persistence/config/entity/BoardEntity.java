package bord.projeto.persistence.config.entity;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

import static bord.projeto.persistence.config.entity.BoardColumnKindEnum.CANCEL;
import static bord.projeto.persistence.config.entity.BoardColumnKindEnum.INITIAL;

@Data
public class BoardEntity {

    private Long id; // Identificador único do board
    private String name; // Nome do board
    private List<BoardColumnEntity> boardColumn = new ArrayList<>(); // Lista de colunas do board

    /**
     * Obtém a coluna inicial do board.
     *
     * @return A coluna de tipo INITIAL, se encontrada
     */
    public BoardColumnEntity getInitialColumn() {
        return getFilteredColumn(bc -> bc.getKind().equals(INITIAL));
    }

    /**
     * Obtém a coluna de cancelamento do board.
     *
     * @return A coluna de tipo CANCEL, se encontrada.
     */
    public BoardColumnEntity getCancelColumn() {
        return getFilteredColumn(bc -> bc.getKind().equals(CANCEL));
    }

    /**
     * Método privado para buscar uma coluna com base em um filtro.
     *
     * @param filter Predicate para filtrar a coluna desejada.
     * @return A primeira coluna que corresponde ao filtro.
     */
    private BoardColumnEntity getFilteredColumn(Predicate<BoardColumnEntity> filter) {
        return boardColumn.stream()
                .filter(filter)
                .findFirst()
                .orElseThrow();
    }
}
