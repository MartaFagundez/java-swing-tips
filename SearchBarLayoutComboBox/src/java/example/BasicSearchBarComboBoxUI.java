// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.awt.image.RescaleOp;
import java.util.Objects;
import javax.swing.*;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import javax.swing.plaf.ComponentUI;

@SuppressWarnings("PMD.TooManyMethods") // This class has too many methods, consider refactoring it.
public class BasicSearchBarComboBoxUI extends SearchBarComboBoxUI {
  protected PopupMenuListener popupMenuListener;
  protected JButton loupeButton;
  protected Action loupeAction = new AbstractAction() {
    @Override public void actionPerformed(ActionEvent e) {
      comboBox.setPopupVisible(false);
      Object o = listBox.getSelectedValue();
      if (Objects.isNull(o)) {
        o = comboBox.getItemAt(0);
      }
      String msg = o + ": " + comboBox.getEditor().getItem();
      JOptionPane.showMessageDialog(comboBox.getRootPane(), msg);
    }
  };

  public static ComponentUI createUI(JComponent c) {
    return new BasicSearchBarComboBoxUI();
  }

  /**
   * {@inheritDoc}
   */
  @Override protected void installDefaults() {
    super.installDefaults();
    // comboBox.setEditable(true);
    comboBox.putClientProperty("JComboBox.isTableCellEditor", Boolean.TRUE);
    // comboBox.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
  }

  /**
   * {@inheritDoc}
   */
  @Override protected void installListeners() {
    super.installListeners();
    popupMenuListener = createPopupMenuListener();
    comboBox.addPopupMenuListener(popupMenuListener);
  }

  /**
   * {@inheritDoc}
   */
  @Override protected void uninstallListeners() {
    super.uninstallListeners();
    comboBox.removePopupMenuListener(popupMenuListener);
  }

  protected final PopupMenuListener createPopupMenuListener() {
    if (Objects.isNull(popupMenuListener)) {
      popupMenuListener = new PopupMenuListener() {
        private String str;
        @Override public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
          JComboBox<?> combo = (JComboBox<?>) e.getSource();
          str = combo.getEditor().getItem().toString();
        }

        @Override public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
          JComboBox<?> combo = (JComboBox<?>) e.getSource();
          Object o = listBox.getSelectedValue();
          if (o instanceof SearchEngine) {
            SearchEngine se = (SearchEngine) o;
            arrowButton.setIcon(se.favicon);
            arrowButton.setRolloverIcon(makeRolloverIcon(se.favicon));
          }
          combo.getEditor().setItem(str);
        }

        @Override public void popupMenuCanceled(PopupMenuEvent e) {
          /* not needed */
        }
      };
    }
    return popupMenuListener;
  }

  // // NullPointerException at BasicComboBoxUI#isNavigationKey(int keyCode, int modifiers)
  // private static class DummyKeyAdapter extends KeyAdapter {
  //   /* dummy */
  // }
  //
  // @Override protected KeyListener createKeyListener() {
  //   if (Objects.isNull(keyListener)) {
  //     keyListener = new DummyKeyAdapter();
  //   }
  //   return keyListener;
  // }

  /**
   * {@inheritDoc}
   */
  @Override protected void configureEditor() {
    // super.configureEditor();
    // Should be in the same state as the JComboBox
    editor.setEnabled(comboBox.isEnabled());
    editor.setFocusable(comboBox.isFocusable());
    editor.setFont(comboBox.getFont());
    // if (Objects.nonNull(focusListener)) {
    //   editor.addFocusListener(focusListener);
    // }
    // editor.addFocusListener(getHandler());
    // comboBox.getEditor().addActionListener(getHandler());
    if (editor instanceof JComponent) {
      // ((JComponent) editor).putClientProperty("doNotCancelPopup", HIDE_POPUP_KEY);
      ((JComponent) editor).setInheritsPopupMenu(true);
    }
    comboBox.configureEditor(comboBox.getEditor(), comboBox.getSelectedItem());
    editor.addPropertyChangeListener(propertyChangeListener);

    ((JComponent) editor).setBorder(BorderFactory.createEmptyBorder(0, 4, 0, 0));
    ((JComponent) editor).getActionMap().put("loupe", loupeAction);
    InputMap im = ((JComponent) editor).getInputMap(JComponent.WHEN_FOCUSED);
    im.put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "loupe");
  }

  /**
   * {@inheritDoc}
   */
  @Override protected JButton createArrowButton() {
    return new TriangleArrowButton();
  }

  /**
   * {@inheritDoc}
   */
  @Override public void configureArrowButton() {
    super.configureArrowButton();
    if (Objects.nonNull(arrowButton)) {
      arrowButton.setBackground(UIManager.getColor("Panel.background"));
      arrowButton.setHorizontalAlignment(SwingConstants.LEFT);
      arrowButton.setOpaque(true);
      arrowButton.setFocusPainted(false);
      arrowButton.setContentAreaFilled(false);
      arrowButton.setBorder(BorderFactory.createCompoundBorder(
          BorderFactory.createMatteBorder(0, 0, 0, 1, new Color(0x7F_9D_B9)),
          BorderFactory.createEmptyBorder(1, 1, 1, 1)));
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override protected void installComponents() {
    // super.installComponents();
    arrowButton = createArrowButton();
    comboBox.add(arrowButton);
    configureArrowButton();

    loupeButton = createLoupeButton();
    comboBox.add(loupeButton);
    configureLoupeButton();

    // if (comboBox.isEditable())
    addEditor();
    comboBox.add(currentValuePane);
  }

  /**
   * {@inheritDoc}
   */
  @Override protected void uninstallComponents() {
    if (loupeButton != null) {
      unconfigureLoupeButton();
    }
    loupeButton = null;
    super.uninstallComponents();
  }

  protected final JButton createLoupeButton() {
    JButton button = new JButton(loupeAction);
    ImageIcon loupe = MainPanel.makeIcon("loupe");
    button.setIcon(loupe);
    button.setRolloverIcon(makeRolloverIcon(loupe));
    return button;
  }

  public final void configureLoupeButton() {
    if (loupeButton != null) {
      loupeButton.setName("ComboBox.loupeButton");
      loupeButton.setBorder(BorderFactory.createEmptyBorder(1, 1, 1, 1));
      loupeButton.setEnabled(comboBox.isEnabled());
      loupeButton.setFocusable(comboBox.isFocusable());
      loupeButton.setOpaque(false);
      loupeButton.setRequestFocusEnabled(false);
      loupeButton.setFocusPainted(false);
      loupeButton.setContentAreaFilled(false);
      // loupeButton.addMouseListener(popup.getMouseListener());
      // loupeButton.addMouseMotionListener(popup.getMouseMotionListener());
      loupeButton.resetKeyboardActions();
      // loupeButton.putClientProperty("doNotCancelPopup", HIDE_POPUP_KEY);
      loupeButton.setInheritsPopupMenu(true);
    }
  }

  public final void unconfigureLoupeButton() {
    if (loupeButton != null) {
      loupeButton.setAction(null);
      // loupeButton.removeMouseListener(popup.getMouseListener());
      // loupeButton.removeMouseMotionListener(popup.getMouseMotionListener());
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override protected ListCellRenderer<SearchEngine> createRenderer() {
    return new SearchEngineListCellRenderer<>();
  }

  /**
   * {@inheritDoc}
   */
  @Override protected LayoutManager createLayoutManager() {
    return new SearchBarLayout();
  }

  protected static ImageIcon makeRolloverIcon(ImageIcon srcIcon) {
    int w = srcIcon.getIconWidth();
    int h = srcIcon.getIconHeight();
    BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
    Graphics2D g2 = img.createGraphics();
    srcIcon.paintIcon(null, g2, 0, 0);
    float[] scaleFactors = {1.2f, 1.2f, 1.2f, 1f};
    float[] offsets = {0f, 0f, 0f, 0f};
    RescaleOp op = new RescaleOp(scaleFactors, offsets, g2.getRenderingHints());
    g2.dispose();
    return new ImageIcon(op.filter(img, null));
  }
}

class SearchEngineListCellRenderer<E extends SearchEngine> implements ListCellRenderer<E> {
  private final ListCellRenderer<? super E> renderer = new DefaultListCellRenderer();

  @Override public Component getListCellRendererComponent(JList<? extends E> list, E value, int index, boolean isSelected, boolean cellHasFocus) {
    Component c = renderer.getListCellRendererComponent(
        list, value, index, isSelected, cellHasFocus);
    if (Objects.nonNull(value) && c instanceof JLabel) {
      JLabel l = (JLabel) c;
      l.setIcon(value.favicon);
      l.setToolTipText(value.url);
    }
    return c;
  }
}

class SearchBarLayout implements LayoutManager {
  @Override public void addLayoutComponent(String name, Component comp) {
    /* not needed */
  }

  @Override public void removeLayoutComponent(Component comp) {
    /* not needed */
  }

  @Override public Dimension preferredLayoutSize(Container parent) {
    return parent.getPreferredSize();
  }

  @Override public Dimension minimumLayoutSize(Container parent) {
    return parent.getMinimumSize();
  }

  @Override public void layoutContainer(Container parent) {
    if (!(parent instanceof JComboBox)) {
      return;
    }
    JComboBox<?> cb = (JComboBox<?>) parent;
    Rectangle r = SwingUtilities.calculateInnerArea(cb, null);

    int arrowSize = 0;
    JButton arrowButton = (JButton) cb.getComponent(0);
    if (Objects.nonNull(arrowButton)) {
      Insets arrowInsets = arrowButton.getInsets();
      int bw = arrowButton.getPreferredSize().width + arrowInsets.left + arrowInsets.right;
      arrowButton.setBounds(r.x, r.y, bw, r.height);
      arrowSize = bw;
    }
    JButton loupeButton = null;
    for (Component c : cb.getComponents()) {
      if ("ComboBox.loupeButton".equals(c.getName())) {
        loupeButton = (JButton) c;
        break;
      }
    }
    int loupeSize = 0;
    if (Objects.nonNull(loupeButton)) {
      loupeSize = r.height;
      loupeButton.setBounds(r.x + r.width - loupeSize, r.y, loupeSize, r.height);
    }
    JTextField editor = (JTextField) cb.getEditor().getEditorComponent();
    if (Objects.nonNull(editor)) {
      editor.setBounds(r.x + arrowSize, r.y, r.width - arrowSize - loupeSize, r.height);
    }
  }
}
