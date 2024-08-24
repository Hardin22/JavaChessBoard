module org.example.javachess {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.bootstrapfx.core;


    exports org.example.javachess.Application;
    opens org.example.javachess.Application to javafx.fxml;
    exports org.example.javachess.Oggetti;
    opens org.example.javachess.Oggetti to javafx.fxml;
    opens org.example.javachess.Controllers to javafx.fxml;
    requires javafx.web;
    requires org.json;
    requires chesslib;
    requires com.fazecast.jSerialComm;
    opens org.example.javachess.Utils to com.fazecast.jSerialComm;


}