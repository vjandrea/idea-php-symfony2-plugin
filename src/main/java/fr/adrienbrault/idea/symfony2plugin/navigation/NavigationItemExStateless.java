package fr.adrienbrault.idea.symfony2plugin.navigation;

import com.intellij.ide.util.PsiNavigationSupport;
import com.intellij.navigation.ItemPresentation;
import com.intellij.navigation.NavigationItem;
import com.intellij.pom.Navigatable;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

/**
 * @author Daniel Espendiller <daniel@espendiller.net>
 */
public class NavigationItemExStateless implements NavigationItem, ItemPresentation {

    @NotNull
    private final PsiElement psiElement;

    @NotNull
    private final String name;

    @NotNull
    private final Icon icon;

    @NotNull
    private final String locationString;

    private NavigationItemExStateless(@NotNull PsiElement psiElement, @NotNull String name, @NotNull Icon icon, @NotNull String locationString) {
        this.psiElement = psiElement;
        this.name = name;
        this.icon = icon;
        this.locationString = locationString;
    }

    @Override
    public @NotNull String getName() {
        return this.name;
    }

    @Nullable
    @Override
    public ItemPresentation getPresentation() {
        return this;
    }

    @Override
    public void navigate(boolean requestFocus) {
        final Navigatable descriptor = PsiNavigationSupport.getInstance().getDescriptor(this.psiElement);
        if (descriptor != null) {
            descriptor.navigate(requestFocus);
        }
    }

    @Override
    public boolean canNavigate() {
        return PsiNavigationSupport.getInstance().canNavigate(this.psiElement);
    }

    @Override
    public boolean canNavigateToSource() {
        return canNavigate();
    }

    @Override
    public String toString() {
        return this.name;
    }

    @Nullable
    @Override
    public String getPresentableText() {
        return name;
    }

    @Override
    public @NotNull String getLocationString() {
        return this.locationString;
    }
    
    @Nullable
    @Override
    public Icon getIcon(boolean b) {
        return icon;
    }

    public static NavigationItemExStateless create(@NotNull PsiElement psiElement, @NotNull String name, @NotNull Icon icon, @NotNull String locationString, boolean appendBundleLocation) {
        String locationPathString = locationString;

        if (appendBundleLocation) {
            PsiFile psiFile = psiElement.getContainingFile();
            if (psiFile != null) {
                locationPathString = locationString + " " + psiFile.getName();

                String bundleName = psiFile.getVirtualFile().getPath();

                if (bundleName.contains("Bundle")) {
                    bundleName = bundleName.substring(0, bundleName.lastIndexOf("Bundle"));
                    if (bundleName.length() > 1 && bundleName.contains("/")) {
                        locationPathString = locationPathString + " " + bundleName.substring(bundleName.lastIndexOf("/") + 1) + "::" + psiFile.getName();
                    }
                }
            }
        }

        return new NavigationItemExStateless(
            psiElement,
            name,
            icon,
            locationPathString
        );
    } 
}

