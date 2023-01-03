package net.gicode.tomb.ui;

import java.util.concurrent.ExecutionException;

import javax.swing.BorderFactory;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.SwingWorker;

import net.gicode.tomb.TombException;

public class RunWithProgressDialog extends JDialog {
  interface TombRunnable {
    public void run() throws TombException;
  }

  private JFrame parentFrame;
  private String errorTitle;

  public RunWithProgressDialog(JFrame parentFrame, String errorTitle) {
    super(parentFrame, true);
    this.parentFrame = parentFrame;
    this.errorTitle = errorTitle;

    JPanel panel = new JPanel();
    panel.setBorder(BorderFactory.createEtchedBorder());
    add(panel);

    JProgressBar progressBar = new JProgressBar();
    progressBar.setIndeterminate(true);
    panel.add(progressBar);

    setUndecorated(true);
    pack();
    setLocationRelativeTo(parentFrame);
  }

  public boolean execute(TombRunnable body) {
    SwingWorker<Boolean, Void> worker = new SwingWorker<Boolean, Void>() {
      @Override
      public Boolean doInBackground() throws TombException {
        body.run();
        return true;
      }

      @Override
      public void done() {
        try {
          get();
        } catch (InterruptedException | ExecutionException e) {
          String reason = e.getMessage();
          Throwable cause = e.getCause();
          if (cause != null) {
            reason = cause.getMessage();
          }
          JOptionPane.showMessageDialog(parentFrame, reason, errorTitle, JOptionPane.ERROR_MESSAGE);
        } finally {
          setVisible(false);
        }
      }
    };

    worker.execute();
    setVisible(true); // Blocks until setVisible(false)
    dispose();

    boolean success = false;
    try {
      success = worker.get();
    } catch (InterruptedException | ExecutionException e) {
      // Handled inside SwingWorker
    }
    return success;
  }
}
