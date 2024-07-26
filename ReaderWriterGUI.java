import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class ReaderWriterGUI extends JFrame {
    private final ReaderWriterMonitor monitor;
    private int readerCount = 0;
    private int writerCount = 0;
    private final JLabel readerLabel = new JLabel("Readers: 0");
    private final JLabel writerLabel = new JLabel("Writers: 0");
    private final JLabel avgReaderWaitLabel = new JLabel("Average Reader Wait Time: 0 ms");
    private final JLabel avgWriterWaitLabel = new JLabel("Average Writer Wait Time: 0 ms");
    private final DefaultListModel<String> readerListModel = new DefaultListModel<>();
    private final DefaultListModel<String> writerListModel = new DefaultListModel<>();
    private final Lock readerLock = new ReentrantLock();
    private final Lock writerLock = new ReentrantLock();

    public ReaderWriterGUI(ReaderWriterMonitor monitor) {
        this.monitor = monitor;
        monitor.setGUI(this);

        setTitle("Reader-Writers Problem");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        JPanel statusPanel = new JPanel(new GridLayout(4, 1));
        statusPanel.add(readerLabel);
        statusPanel.add(writerLabel);
        statusPanel.add(avgReaderWaitLabel);
        statusPanel.add(avgWriterWaitLabel);
        add(statusPanel, BorderLayout.NORTH);

        JList<String> readerList = new JList<>(readerListModel);
        readerList.setCellRenderer(new StatusListCellRenderer());
        add(new JScrollPane(readerList), BorderLayout.WEST);

        JList<String> writerList = new JList<>(writerListModel);
        writerList.setCellRenderer(new StatusListCellRenderer());
        add(new JScrollPane(writerList), BorderLayout.EAST);

        JPanel controlPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.gridx = 0;
        gbc.gridy = 0;

        JButton startReaderButton = new JButton("Start Reader");
        startReaderButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                startReader();
            }
        });
        controlPanel.add(startReaderButton, gbc);

        gbc.gridx = 1;
        JButton stopReaderButton = new JButton("Stop Reader");
        stopReaderButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                stopReader();
            }
        });
        controlPanel.add(stopReaderButton, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        JButton startWriterButton = new JButton("Start Writer");
        startWriterButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                startWriter();
            }
        });
        controlPanel.add(startWriterButton, gbc);

        gbc.gridx = 1;
        JButton stopWriterButton = new JButton("Stop Writer");
        stopWriterButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                stopWriter();
            }
        });
        controlPanel.add(stopWriterButton, gbc);

        add(controlPanel, BorderLayout.CENTER);
    }

    public void updateReaderCount(int count) {
        SwingUtilities.invokeLater(() -> readerLabel.setText("Readers: " + Math.max(count, 0)));
    }

    public void updateWriterCount(int count) {
        SwingUtilities.invokeLater(() -> writerLabel.setText("Writers: " + Math.max(count, 0)));
    }

    public void updateAverageWaitTimes() {
        SwingUtilities.invokeLater(() -> {
            avgReaderWaitLabel.setText("Average Reader Wait Time: " + monitor.getAverageReaderWaitTime() + " ms");
            avgWriterWaitLabel.setText("Average Writer Wait Time: " + monitor.getAverageWriterWaitTime() + " ms");
        });
    }

    public void updateReaderStatus(int readerId, String status, String color, long waitTime) {
        SwingUtilities.invokeLater(() -> {
            String statusText = "Reader " + readerId + ": " + status + (waitTime > 0 ? " (Waited: " + waitTime + " ms)" : "");
            if (readerListModel.size() >= readerId) {
                readerListModel.setElementAt(statusText, readerId - 1);
            } else {
                readerListModel.addElement(statusText);
            }
        });
    }

    public void updateWriterStatus(int writerId, String status, String color, long waitTime) {
        SwingUtilities.invokeLater(() -> {
            String statusText = "Writer " + writerId + ": " + status + (waitTime > 0 ? " (Waited: " + waitTime + " ms)" : "");
            if (writerListModel.size() >= writerId) {
                writerListModel.setElementAt(statusText, writerId - 1);
            } else {
                writerListModel.addElement(statusText);
            }
        });
    }

    private void startReader() {
        readerCount++;
        new Thread(() -> {
            try {
                monitor.startRead(readerCount);
                Thread.sleep(2000); // Simulate reading time
                monitor.stopRead(readerCount);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();
    }

    private void stopReader() {
        if (readerCount > 0) {
            new Thread(() -> {
                try {
                    monitor.stopRead(readerCount);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                readerCount--;
            }).start();
        }
    }

    private void startWriter() {
        writerCount++;
        new Thread(() -> {
            try {
                monitor.startWrite(writerCount);
                Thread.sleep(2000); // Simulate writing time
                monitor.stopWrite(writerCount);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();
    }

    private void stopWriter() {
        if (writerCount > 0) {
            new Thread(() -> {
                monitor.stopWrite(writerCount);
                writerCount--;
            }).start();
        }
    }

    public static void main(String[] args) {
        ReaderWriterMonitor monitor = new ReaderWriterMonitor();
        ReaderWriterGUI gui = new ReaderWriterGUI(monitor);
        gui.setVisible(true);
    }

    class StatusListCellRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            String text = (String) value;
            if (text.contains("Waiting")) {
                label.setBackground(Color.ORANGE);
            } else if (text.contains("Reading") || text.contains("Writing")) {
                label.setBackground(Color.GREEN);
            } else if (text.contains("Stopped")) {
                label.setBackground(Color.RED);
            }
            return label;
        }
    }
}
