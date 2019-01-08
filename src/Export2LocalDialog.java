import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataKeys;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.ui.ToolbarDecorator;
import com.intellij.ui.components.JBList;
import org.apache.commons.lang3.StringUtils;

import javax.swing.*;
import java.awt.event.*;
import java.io.File;
import java.io.IOException;

public class Export2LocalDialog extends JDialog {

    private JPanel contentPane;
    private JButton buttonOK;
    private JButton buttonCancel;

    private JTextField textField;
    private JButton fileChooseBtn;
    private JPanel filePanel;
	private JCheckBox javaCheckBox;
	private JCheckBox classCheckBox;
    private AnActionEvent event;
    private JBList fieldList;

	Export2LocalDialog(final AnActionEvent event) {
        this.event = event;
        setTitle("Export to Local");

        setContentPane(contentPane);
        setModal(true);
        getRootPane().setDefaultButton(buttonOK);

        buttonOK.addActionListener(new ActionListener() {
        	@Override
            public void actionPerformed(ActionEvent e) {
                onOK();
            }
        });

        buttonCancel.addActionListener(new ActionListener() {
        	@Override
            public void actionPerformed(ActionEvent e) {
                onCancel();
            }
        });

        // call onCancel() when cross is clicked
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                onCancel();
            }
        });

        // call onCancel() on ESCAPE
        contentPane.registerKeyboardAction(new ActionListener() {
        	@Override
            public void actionPerformed(ActionEvent e) {
                onCancel();
            }
        }, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);

        // 保存路径按钮事件
        fileChooseBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String userDir = System.getProperty("user.home");
                JFileChooser fileChooser = new JFileChooser(userDir + "/Desktop");
                fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                int flag = fileChooser.showOpenDialog(null);
                if (flag == JFileChooser.APPROVE_OPTION) {
                    textField.setText(fileChooser.getSelectedFile().getAbsolutePath());
                }
            }
        });

    }

    private void onOK() {
        // 条件校验
        if (null == textField.getText() || "".equals(textField.getText())) {
            Messages.showErrorDialog(this, "Please Select Save Path!", "Error");
            return;
        }

        ListModel<VirtualFile> model = fieldList.getModel();
        if (model.getSize() == 0) {
            Messages.showErrorDialog(this, "Please Select Export File!", "Error");
            return;
        }

        try {
            // 模块对象
            Module module = event.getData(DataKeys.MODULE);
            String targetPath = "/target/classes";
			String moduleName = module.getName();
			String srcPath = moduleName +"/src/main/java";
			String resPath = moduleName +"/src/main/resources";
            // 导出目录
            String exportPath = textField.getText()+"/"+moduleName;
			boolean javaCheckBoxSelected = javaCheckBox.isSelected();
			boolean classCheckBoxSelected = classCheckBox.isSelected();
			for (int i = 0; i < model.getSize(); i++) {
                VirtualFile element = model.getElementAt(i);
                String elementPath = element.getPath();
				//判断是否为java文件或目录
				int srcPathPos = elementPath.indexOf(srcPath);
				if(srcPathPos !=-1){
					String packPath = StringUtils.substring(elementPath,srcPathPos+srcPath.length()+1);
					//src
					exportJavaSource(javaCheckBoxSelected, elementPath, exportPath +"/src/main/java/"+ packPath);
					//class
					exportJavaClass(targetPath, classCheckBoxSelected, elementPath, exportPath +targetPath+"/"+ packPath);
				}else{
					int resPathPos = elementPath.indexOf(resPath);
					String packPath = StringUtils.substring(elementPath,elementPath.indexOf(moduleName)+moduleName.length()+1);
					String toResPath = exportPath+"/"+packPath;
					if(resPathPos!=-1 ){
						if(classCheckBoxSelected){
							toResPath = (exportPath + "/" + packPath).replace(resPath.replace(moduleName,""), targetPath);
							FileUtil.copyFileOrDir(new File(elementPath),new File(toResPath));
						}
						if(javaCheckBoxSelected){
							toResPath = exportPath+"/"+packPath;
							FileUtil.copyFileOrDir(new File(elementPath),new File(toResPath));
						}
					}else{
						FileUtil.copyFileOrDir(new File(elementPath),new File(toResPath));
					}
				}

            }
        } catch (Exception e) {
            Messages.showErrorDialog(this, "Export to Local Error!", "Error");
            e.printStackTrace();
        }

        // add your code here
        dispose();
    }

	private void exportJavaClass(String targetPath,boolean classCheckBoxSelected, String elementPath, String packPath) throws IOException {
		if(classCheckBoxSelected){
			String toTargetPath = StringUtils.replace(elementPath, "/src/main/java", targetPath);
			if(toTargetPath.endsWith(".java")){
				toTargetPath = toTargetPath.replace(".java",".class");
				packPath = packPath.replace(".java",".class");
			}
			File targetTo = new File(packPath);
			File targetFile = new File(toTargetPath);
			FileUtil.copyFileOrDir(targetFile, targetTo);
		}
	}

	private void exportJavaSource(boolean javaCheckBoxSelected, String elementPath, String packPath) throws IOException {
		if(javaCheckBoxSelected){
			File srcFrom = new File(elementPath);
			File srcTo = new File(packPath);
			FileUtil.copyFileOrDir(srcFrom, srcTo);
		}
	}

	private void onCancel() {
        // add your code here if necessary
        dispose();
    }

    private void createUIComponents() {
        VirtualFile[] data = event.getData(DataKeys.VIRTUAL_FILE_ARRAY);
        fieldList = new JBList(data);
        fieldList.setEmptyText("No File Selected!");
        ToolbarDecorator decorator = ToolbarDecorator.createDecorator(fieldList);
        filePanel = decorator.createPanel();
    }
}
