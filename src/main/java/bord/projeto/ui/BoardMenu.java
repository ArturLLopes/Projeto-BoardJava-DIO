package bord.projeto.ui;

import bord.projeto.persistence.config.entity.BoardEntity;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class BoardMenu {

    private final BoardEntity entity;

    public void execute() {
        System.out.println("Board selecionado!");
    }
}
