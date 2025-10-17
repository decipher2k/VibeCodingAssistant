/* Copyright 2025 Dennis Michael Heine */
package com.vibecoding.wizard;

import javax.swing.*;
import javax.swing.tree.*;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.*;
import java.awt.event.ActionEvent;
import java.util.List;

/**
 * Panel containing a tree view of modules with add/delete buttons
 * and drag-and-drop support.
 */
public final class ModuleTreePanel extends JPanel {
    private final JTree tree;
    private final DefaultTreeModel treeModel;
    private final DefaultMutableTreeNode rootNode;
    private final IDEController controller;
    
    public ModuleTreePanel(IDEController controller) {
        this.controller = controller;
        this.rootNode = new DefaultMutableTreeNode("Project");
        this.treeModel = new DefaultTreeModel(rootNode);
        this.tree = new JTree(treeModel);
        
        setLayout(new BorderLayout());
        setOpaque(true);
        setBackground(ThemeManager.getBackgroundColor());
        setBorder(BorderFactory.createEmptyBorder(8, 10, 10, 10));  // Match main panel padding exactly
        setMinimumSize(new Dimension(200, 400));  // Ensure minimum size for split pane
        
        // Configure tree with modern styling
        tree.setRootVisible(true);
        tree.setShowsRootHandles(true);
        tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
        tree.setCellRenderer(new ModuleTreeCellRenderer());
        tree.setBackground(Color.WHITE);  // White background like main panels
        tree.setForeground(ThemeManager.getTextColor());
        tree.setRowHeight(24);  // Comfortable row height
        tree.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));  // Inner tree padding
        
        // Remove tree lines for cleaner look
        tree.putClientProperty("JTree.lineStyle", "None");
        
        // Enable drag and drop
        tree.setDragEnabled(true);
        tree.setDropMode(DropMode.ON_OR_INSERT);
        tree.setTransferHandler(new ModuleTreeTransferHandler());
        
        // Add selection listener
        tree.addTreeSelectionListener(e -> {
            DefaultMutableTreeNode node = (DefaultMutableTreeNode) tree.getLastSelectedPathComponent();
            if (node != null && node != rootNode) {
                Module module = (Module) node.getUserObject();
                controller.selectModule(module);
            }
        });
        
        // Create context menu
        JPopupMenu contextMenu = createContextMenu();
        tree.setComponentPopupMenu(contextMenu);
        
        // Add tree to scroll pane
        JScrollPane scrollPane = new JScrollPane(tree);
        scrollPane.setBorder(null);  // No border on scroll pane itself
        scrollPane.getViewport().setBackground(Color.WHITE);
        scrollPane.setBackground(Color.WHITE);
        
        // Create button panel with vertical BoxLayout for stacked buttons
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.Y_AXIS));
        buttonPanel.setBackground(Color.WHITE);  // White background to match container
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(5, 8, 8, 8));  // Padding inside white container
        
        JButton addButton = new JButton("Add Module");
        addButton.setMaximumSize(new Dimension(Integer.MAX_VALUE, addButton.getPreferredSize().height));
        addButton.setToolTipText("Add Module");
        addButton.addActionListener(e -> handleAddModule());
        
        JButton addSubButton = new JButton("Add Submodule");
        addSubButton.setMaximumSize(new Dimension(Integer.MAX_VALUE, addSubButton.getPreferredSize().height));
        addSubButton.setToolTipText("Add Submodule");
        addSubButton.addActionListener(e -> handleAddSubmodule());
        
        JButton deleteButton = new JButton("Delete");
        deleteButton.setMaximumSize(new Dimension(Integer.MAX_VALUE, deleteButton.getPreferredSize().height));
        deleteButton.setToolTipText("Delete Module");
        deleteButton.addActionListener(e -> handleDeleteModule());
        
        buttonPanel.add(addButton);
        buttonPanel.add(Box.createVerticalStrut(3));
        buttonPanel.add(addSubButton);
        buttonPanel.add(Box.createVerticalStrut(3));
        buttonPanel.add(deleteButton);
        buttonPanel.add(Box.createVerticalGlue());  // Push buttons to the top
        
        // Create white content container that holds both tree and buttons
        JPanel whiteContainer = new JPanel(new BorderLayout());
        whiteContainer.setBackground(Color.WHITE);
        whiteContainer.setBorder(BorderFactory.createLineBorder(new Color(0xA19F9D), 1));  // Border around entire white area
        whiteContainer.add(scrollPane, BorderLayout.CENTER);
        whiteContainer.add(buttonPanel, BorderLayout.SOUTH);
        
        // Create transparent wrapper with padding to position the white container
        // This padding matches where the white content starts in the main panel
        JPanel positionWrapper = new JPanel(new BorderLayout());
        positionWrapper.setOpaque(false);  // Transparent so grey background shows through
        positionWrapper.setBorder(BorderFactory.createEmptyBorder(8, 24, 8, 24));  // Match form content padding (8,24,8,24)
        positionWrapper.add(whiteContainer, BorderLayout.CENTER);
        
        add(positionWrapper, BorderLayout.CENTER);
    }
    
    /**
     * Creates the context menu for tree nodes.
     */
    private JPopupMenu createContextMenu() {
        JPopupMenu menu = new JPopupMenu();
        
        JMenuItem addModuleItem = new JMenuItem("Add Module");
        addModuleItem.addActionListener(e -> handleAddModule());
        menu.add(addModuleItem);
        
        JMenuItem addSubmoduleItem = new JMenuItem("Add Submodule");
        addSubmoduleItem.addActionListener(e -> handleAddSubmodule());
        menu.add(addSubmoduleItem);
        
        menu.addSeparator();
        
        JMenuItem renameItem = new JMenuItem("Rename");
        renameItem.addActionListener(e -> handleRenameModule());
        menu.add(renameItem);
        
        JMenuItem deleteItem = new JMenuItem("Delete");
        deleteItem.addActionListener(e -> handleDeleteModule());
        menu.add(deleteItem);
        
        menu.addSeparator();
        
        JMenuItem moveUpItem = new JMenuItem("Move Up");
        moveUpItem.addActionListener(e -> handleMoveUp());
        menu.add(moveUpItem);
        
        JMenuItem moveDownItem = new JMenuItem("Move Down");
        moveDownItem.addActionListener(e -> handleMoveDown());
        menu.add(moveDownItem);
        
        menu.addSeparator();
        
        JMenuItem setMainItem = new JMenuItem("Set as Main Module");
        setMainItem.addActionListener(e -> handleSetMainModule());
        menu.add(setMainItem);
        
        return menu;
    }
    
    /**
     * Handles adding a new root module.
     */
    private void handleAddModule() {
        String name = JOptionPane.showInputDialog(this, 
            "Enter module name:", 
            "New Module", 
            JOptionPane.PLAIN_MESSAGE);
        
        if (name != null && !name.trim().isEmpty()) {
            controller.addRootModule(name.trim());
        }
    }
    
    /**
     * Handles adding a submodule to the selected module.
     */
    private void handleAddSubmodule() {
        DefaultMutableTreeNode selectedNode = (DefaultMutableTreeNode) tree.getLastSelectedPathComponent();
        
        if (selectedNode == null) {
            JOptionPane.showMessageDialog(this,
                "Please select a module first.",
                "No Selection",
                JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        String name = JOptionPane.showInputDialog(this,
            "Enter submodule name:",
            "New Submodule",
            JOptionPane.PLAIN_MESSAGE);
        
        if (name != null && !name.trim().isEmpty()) {
            if (selectedNode == rootNode) {
                controller.addRootModule(name.trim());
            } else {
                Module parent = (Module) selectedNode.getUserObject();
                controller.addSubmodule(parent, name.trim());
            }
        }
    }
    
    /**
     * Handles renaming the selected module.
     */
    private void handleRenameModule() {
        DefaultMutableTreeNode selectedNode = (DefaultMutableTreeNode) tree.getLastSelectedPathComponent();
        
        if (selectedNode == null || selectedNode == rootNode) {
            return;
        }
        
        Module module = (Module) selectedNode.getUserObject();
        String newName = JOptionPane.showInputDialog(this,
            "Enter new name:",
            module.getName());
        
        if (newName != null && !newName.trim().isEmpty()) {
            controller.renameModule(module, newName.trim());
        }
    }
    
    /**
     * Handles deleting the selected module.
     */
    private void handleDeleteModule() {
        DefaultMutableTreeNode selectedNode = (DefaultMutableTreeNode) tree.getLastSelectedPathComponent();
        
        if (selectedNode == null || selectedNode == rootNode) {
            return;
        }
        
        Module module = (Module) selectedNode.getUserObject();
        int confirm = JOptionPane.showConfirmDialog(this,
            "Delete module '" + module.getName() + "' and all its submodules?",
            "Confirm Delete",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE);
        
        if (confirm == JOptionPane.YES_OPTION) {
            controller.deleteModule(module);
        }
    }
    
    /**
     * Handles moving the selected module up in its parent's list.
     */
    private void handleMoveUp() {
        DefaultMutableTreeNode selectedNode = (DefaultMutableTreeNode) tree.getLastSelectedPathComponent();
        
        if (selectedNode == null || selectedNode == rootNode) {
            return;
        }
        
        Module module = (Module) selectedNode.getUserObject();
        controller.moveModuleUp(module);
    }
    
    /**
     * Handles moving the selected module down in its parent's list.
     */
    private void handleMoveDown() {
        DefaultMutableTreeNode selectedNode = (DefaultMutableTreeNode) tree.getLastSelectedPathComponent();
        
        if (selectedNode == null || selectedNode == rootNode) {
            return;
        }
        
        Module module = (Module) selectedNode.getUserObject();
        controller.moveModuleDown(module);
    }
    
    /**
     * Handles setting the selected module as the main module.
     */
    private void handleSetMainModule() {
        DefaultMutableTreeNode selectedNode = (DefaultMutableTreeNode) tree.getLastSelectedPathComponent();
        
        if (selectedNode == null || selectedNode == rootNode) {
            return;
        }
        
        Module module = (Module) selectedNode.getUserObject();
        controller.setMainModule(module);
        treeModel.nodeChanged(rootNode); // Refresh to show main module indicator
        refreshTree();
    }
    
    /**
     * Rebuilds the tree from the current project state.
     */
    public void refreshTree() {
        rootNode.removeAllChildren();
        
        IDEProject project = controller.getCurrentProject();
        if (project != null) {
            for (Module module : project.getRootModules()) {
                DefaultMutableTreeNode node = createTreeNode(module);
                rootNode.add(node);
            }
        }
        
        treeModel.reload();
        expandAll();
    }
    
    /**
     * Creates a tree node for a module and all its children recursively.
     */
    private DefaultMutableTreeNode createTreeNode(Module module) {
        DefaultMutableTreeNode node = new DefaultMutableTreeNode(module);
        
        for (Module sub : module.getSubModules()) {
            node.add(createTreeNode(sub));
        }
        
        return node;
    }
    
    /**
     * Expands all nodes in the tree.
     */
    private void expandAll() {
        for (int i = 0; i < tree.getRowCount(); i++) {
            tree.expandRow(i);
        }
    }
    
    /**
     * Selects a module in the tree.
     */
    public void selectModule(Module module) {
        if (module == null) {
            tree.clearSelection();
            return;
        }
        
        DefaultMutableTreeNode node = findNode(rootNode, module);
        if (node != null) {
            TreePath path = new TreePath(node.getPath());
            tree.setSelectionPath(path);
            tree.scrollPathToVisible(path);
        }
    }
    
    /**
     * Finds a tree node containing the given module.
     */
    private DefaultMutableTreeNode findNode(DefaultMutableTreeNode searchRoot, Module module) {
        if (searchRoot.getUserObject() instanceof Module) {
            Module nodeModule = (Module) searchRoot.getUserObject();
            if (nodeModule.equals(module)) {
                return searchRoot;
            }
        }
        
        for (int i = 0; i < searchRoot.getChildCount(); i++) {
            DefaultMutableTreeNode child = (DefaultMutableTreeNode) searchRoot.getChildAt(i);
            DefaultMutableTreeNode result = findNode(child, module);
            if (result != null) {
                return result;
            }
        }
        
        return null;
    }
    
    /**
     * Custom cell renderer to show module icons and main module indicator.
     */
    private class ModuleTreeCellRenderer extends DefaultTreeCellRenderer {
        
        public ModuleTreeCellRenderer() {
            setBackgroundNonSelectionColor(Color.WHITE);
            setBackgroundSelectionColor(new Color(0x5B7FD7));  // Accent color for selection
            setTextNonSelectionColor(ThemeManager.getTextColor());
            setTextSelectionColor(Color.WHITE);
            setBorderSelectionColor(null);  // No border around selection
            setOpaque(false);
        }
        
        @Override
        public Component getTreeCellRendererComponent(JTree tree, Object value,
                boolean selected, boolean expanded, boolean leaf, int row, boolean hasFocus) {
            
            super.getTreeCellRendererComponent(tree, value, selected, expanded, leaf, row, hasFocus);
            
            DefaultMutableTreeNode node = (DefaultMutableTreeNode) value;
            
            if (node.getUserObject() instanceof Module) {
                Module module = (Module) node.getUserObject();
                IDEProject project = controller.getCurrentProject();
                
                String displayText = module.getName();
                if (project != null && module.equals(project.getMainModule())) {
                    displayText += " [MAIN]";
                    if (!selected) {
                        setForeground(new Color(0x107C10));  // Success green color for main module
                    }
                }
                
                setText(displayText);
            }
            
            // Remove focus border
            setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
            
            return this;
        }
    }
    
    /**
     * Transfer handler for drag and drop operations.
     */
    private class ModuleTreeTransferHandler extends TransferHandler {
        private final DataFlavor nodesFlavor = new DataFlavor(Module.class, "Module");
        
        @Override
        public int getSourceActions(JComponent c) {
            return MOVE;
        }
        
        @Override
        protected Transferable createTransferable(JComponent c) {
            JTree tree = (JTree) c;
            TreePath path = tree.getSelectionPath();
            if (path != null) {
                DefaultMutableTreeNode node = (DefaultMutableTreeNode) path.getLastPathComponent();
                if (node != rootNode && node.getUserObject() instanceof Module) {
                    Module module = (Module) node.getUserObject();
                    return new ModuleTransferable(module);
                }
            }
            return null;
        }
        
        @Override
        public boolean canImport(TransferSupport support) {
            if (!support.isDrop() || !support.isDataFlavorSupported(nodesFlavor)) {
                return false;
            }
            
            JTree.DropLocation dropLocation = (JTree.DropLocation) support.getDropLocation();
            TreePath path = dropLocation.getPath();
            
            if (path == null) {
                return false;
            }
            
            try {
                Module draggedModule = (Module) support.getTransferable().getTransferData(nodesFlavor);
                DefaultMutableTreeNode targetNode = (DefaultMutableTreeNode) path.getLastPathComponent();
                
                // Can't drop on itself
                if (targetNode.getUserObject() == draggedModule) {
                    return false;
                }
                
                // Can't drop on a descendant
                if (targetNode.getUserObject() instanceof Module) {
                    Module targetModule = (Module) targetNode.getUserObject();
                    if (draggedModule.isAncestorOf(targetModule)) {
                        return false;
                    }
                }
                
                return true;
            } catch (Exception e) {
                return false;
            }
        }
        
        @Override
        public boolean importData(TransferSupport support) {
            if (!canImport(support)) {
                return false;
            }
            
            try {
                Module draggedModule = (Module) support.getTransferable().getTransferData(nodesFlavor);
                JTree.DropLocation dropLocation = (JTree.DropLocation) support.getDropLocation();
                TreePath path = dropLocation.getPath();
                DefaultMutableTreeNode targetNode = (DefaultMutableTreeNode) path.getLastPathComponent();
                int childIndex = dropLocation.getChildIndex();
                
                if (targetNode == rootNode) {
                    // Drop on root - make it a root module
                    controller.moveModuleToRoot(draggedModule, childIndex);
                } else if (targetNode.getUserObject() instanceof Module) {
                    Module targetModule = (Module) targetNode.getUserObject();
                    if (childIndex == -1) {
                        // Drop on module - make it a child
                        controller.moveModuleToParent(draggedModule, targetModule, -1);
                    } else {
                        // Drop between modules
                        controller.moveModuleToParent(draggedModule, targetModule, childIndex);
                    }
                }
                
                return true;
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        }
    }
    
    /**
     * Transferable wrapper for Module objects.
     */
    private class ModuleTransferable implements Transferable {
        private final Module module;
        private final DataFlavor flavor = new DataFlavor(Module.class, "Module");
        
        public ModuleTransferable(Module module) {
            this.module = module;
        }
        
        @Override
        public DataFlavor[] getTransferDataFlavors() {
            return new DataFlavor[] { flavor };
        }
        
        @Override
        public boolean isDataFlavorSupported(DataFlavor flavor) {
            return this.flavor.equals(flavor);
        }
        
        @Override
        public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException {
            if (!isDataFlavorSupported(flavor)) {
                throw new UnsupportedFlavorException(flavor);
            }
            return module;
        }
    }
}
