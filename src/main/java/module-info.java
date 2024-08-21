module org.example.javachess {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.bootstrapfx.core;

    opens org.example.javachess to javafx.fxml;
    exports org.example.javachess;
    exports org.example.javachess.Application;
    opens org.example.javachess.Application to javafx.fxml;
    exports org.example.javachess.Oggetti;
    opens org.example.javachess.Oggetti to javafx.fxml;
    opens org.example.javachess.Controllers to javafx.fxml;

    requires chesslib;
}