package org.example.javachess.Utils;

import com.fazecast.jSerialComm.SerialPort;
import com.fazecast.jSerialComm.SerialPortDataListener;
import com.fazecast.jSerialComm.SerialPortEvent;

public class ArduinoSerialReader {

    public static void main(String[] args) {
        // Specifica direttamente la porta che vuoi usare
        SerialPort comPort = SerialPort.getCommPort("/dev/cu.usbmodem31301");
        comPort.setBaudRate(9600);

        if (comPort.openPort()) {
            System.out.println("Porta seriale aperta.");
        } else {
            System.out.println("Errore nell'apertura della porta seriale.");
            return;
        }

        StringBuilder dataBuffer = new StringBuilder(); // Buffer per accumulare i dati

        comPort.addDataListener(new SerialPortDataListener() {
            @Override
            public int getListeningEvents() {
                return SerialPort.LISTENING_EVENT_DATA_AVAILABLE;
            }

            @Override
            public void serialEvent(SerialPortEvent event) {
                if (event.getEventType() != SerialPort.LISTENING_EVENT_DATA_AVAILABLE)
                    return;

                byte[] newData = new byte[comPort.bytesAvailable()];
                comPort.readBytes(newData, newData.length);

                for (byte b : newData) {
                    char c = (char) b;
                    if (c == '\n') {  // Se viene rilevato un carattere di fine linea
                        String mossa = dataBuffer.toString().trim(); // Converti il buffer in stringa
                        System.out.println("Mossa ricevuta: " + mossa); // Mostra la mossa
                        dataBuffer.setLength(0); // Resetta il buffer
                    } else {
                        dataBuffer.append(c); // Aggiungi il carattere al buffer
                    }
                }
            }
        });
    }
}
