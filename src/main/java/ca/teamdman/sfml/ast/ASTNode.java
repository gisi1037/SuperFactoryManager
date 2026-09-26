package ca.teamdman.sfml.ast;

import java.util.Collections;
import java.util.List;

public interface ASTNode {

    default List<Statement> getStatements() {
        return Collections.emptyList();
    }
}
