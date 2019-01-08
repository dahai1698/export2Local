import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataKeys;
import com.intellij.openapi.vfs.VirtualFile;

/**
 * @author summer
 */
public class Export2LoalAction extends AnAction {

    @Override
    public void actionPerformed(AnActionEvent e) {
        Export2LocalDialog dialog = new Export2LocalDialog(e);
        dialog.setSize(628, 421);
        dialog.setLocationRelativeTo(null);
        dialog.setVisible(true);
        dialog.requestFocus();
    }
}
