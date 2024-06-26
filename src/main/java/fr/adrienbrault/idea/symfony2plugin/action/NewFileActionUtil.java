package fr.adrienbrault.idea.symfony2plugin.action;

import com.intellij.ide.IdeView;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.actionSystem.LangDataKeys;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.io.StreamUtil;
import com.intellij.psi.PsiDirectory;
import com.jetbrains.php.roots.PhpNamespaceCompositeProvider;
import fr.adrienbrault.idea.symfony2plugin.util.PhpElementsUtil;
import fr.adrienbrault.idea.symfony2plugin.util.StringUtils;
import fr.adrienbrault.idea.symfony2plugin.util.SymfonyBundleUtil;
import fr.adrienbrault.idea.symfony2plugin.util.SymfonyCommandUtil;
import fr.adrienbrault.idea.symfony2plugin.util.dict.SymfonyBundle;
import fr.adrienbrault.idea.symfony2plugin.util.dict.SymfonyCommand;
import fr.adrienbrault.idea.symfony2plugin.util.psi.PhpBundleFileFactory;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.*;

/**
 * @author Daniel Espendiller <daniel@espendiller.net>
 */
public class NewFileActionUtil {

    public static PsiDirectory getSelectedDirectoryFromAction(@NotNull AnActionEvent event) {
        DataContext dataContext = event.getDataContext();
        IdeView view = LangDataKeys.IDE_VIEW.getData(dataContext);
        if (view == null) {
            return null;
        }

        PsiDirectory @NotNull [] directories = view.getDirectories();
        return directories.length == 0 ? null : directories[0];

    }

    public static boolean isInGivenDirectoryScope(@NotNull AnActionEvent event, @NotNull String... directoryName) {
        DataContext dataContext = event.getDataContext();
        IdeView view = LangDataKeys.IDE_VIEW.getData(dataContext);
        if (view == null) {
            return false;
        }

        PsiDirectory @NotNull [] directories = view.getDirectories();
        if (directories.length == 0) {
            return false;
        }

        List<@NotNull String> list = Arrays.asList(directoryName);

        if (list.contains(directories[0].getName())) {
            return true;
        }

        PsiDirectory parent = directories[0].getParent();
        if (parent != null && list.contains(parent.getName())) {
            return true;
        }

        return false;
    }

    public static String guessCommandTemplateType(@NotNull Project project) {
        if (PhpElementsUtil.getClassInterface(project, "\\Symfony\\Component\\Console\\Attribute\\AsCommand") != null) {
            return "command_attributes";
        }

        boolean isDefaultName = PhpElementsUtil.getClassesInterface(project, "\\Symfony\\Component\\Console\\Command\\Command")
            .stream()
            .anyMatch(phpClass -> phpClass.findFieldByName("defaultName", false) != null);

        if (isDefaultName) {
            return "command_property";
        }

        return "command_configure";
    }

    public static String guessControllerTemplateType(@NotNull Project project) {
        if (PhpElementsUtil.getClassInterface(project, "\\Symfony\\Component\\Routing\\Attribute\\Route") != null) {
            return "controller_attributes";
        }

        return "controller_annotations";
    }

    public static String getCommandPrefix(@NotNull PsiDirectory psiDirectory) {
        List<String> strings = PhpNamespaceCompositeProvider.INSTANCE.suggestNamespaces(psiDirectory).stream().filter(s -> !s.isBlank()).toList();
        if (!strings.isEmpty()) {
            Collection<SymfonyCommand> commands = SymfonyCommandUtil.getCommands(psiDirectory.getProject());

            LinkedHashMap<String, Integer> sortedMap = new LinkedHashMap<>();
            for (String namespace : strings) {
                namespace = "\\" + org.apache.commons.lang3.StringUtils.strip(namespace, "\\") + "\\";

                for (SymfonyCommand command : commands) {
                    if (command.getPhpClass().getFQN().startsWith(namespace)) {
                        String name = command.getName();
                        int i = name.indexOf(":");
                        if (i > 0) {
                            String key = name.substring(0, i);
                            sortedMap.put(key, sortedMap.getOrDefault(key, 0));
                        }
                    }
                }
            }

            if (!sortedMap.isEmpty()) {
                List<Map.Entry<String, Integer>> list = new ArrayList<>(sortedMap.entrySet()
                    .stream()
                    .sorted(Map.Entry.comparingByValue())
                    .toList());

                Collections.reverse(list);

                return list.get(0).getKey();
            }
        }

        for (SymfonyBundle bundle : new SymfonyBundleUtil(psiDirectory.getProject()).getBundles()) {
            if (bundle.isInBundle(psiDirectory.getVirtualFile())) {
                String name = bundle.getName();
                if (name.endsWith("Bundle")) {
                    name = name.substring(0, name.length() - "Bundle".length());
                }

                String underscore = StringUtils.underscore(name);
                if (!underscore.isBlank()) {
                    return underscore;
                }
            }
        }

        return "app";
    }


    @Nullable
    public static String getFileTemplateContent(@NotNull String filename) {
        try {
            // replace on windows, just for secure reasons
            return StreamUtil.readText(PhpBundleFileFactory.class.getResourceAsStream(filename), "UTF-8").replace("\r\n", "\n");
        } catch (IOException e) {
            return null;
        }
    }
}
