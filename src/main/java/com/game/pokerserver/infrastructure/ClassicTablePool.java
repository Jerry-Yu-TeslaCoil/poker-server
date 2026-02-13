package com.game.pokerserver.infrastructure;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.stereotype.Component;
import table.CardTable;
import table.impl.ClassicTable;

import java.util.Stack;

@Component
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ClassicTablePool {

    private int defaultSize;
    private int maxSize;

    private final Stack<CardTable> tables = new Stack<>();

    public CardTable applyTable() {
        return new ClassicTable();
    }

    private synchronized CardTable getTable() {
        return tables.pop();
    }
}
