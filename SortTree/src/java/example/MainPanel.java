// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;
import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.MutableTreeNode;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout());
    DefaultMutableTreeNode root = TreeUtil.makeTreeRoot();
    JTree tree = new JTree(new DefaultTreeModel(TreeUtil.makeTreeRoot()));
    // JRadioButton sort0 = new JRadioButton("0: bubble sort");
    JRadioButton sort1 = new JRadioButton("1: bubble sort");
    JRadioButton sort2 = new JRadioButton("2: selection sort");
    // JRadioButton sort3 = new JRadioButton("3: iterative merge sort"); // JDK 1.6.0
    JRadioButton sort3 = new JRadioButton("3: TimSort"); // JDK 1.7.0
    JRadioButton reset = new JRadioButton("reset");

    JPanel box = new JPanel(new GridLayout(2, 2));
    ActionListener listener = e -> {
      JRadioButton check = (JRadioButton) e.getSource();
      if (check.equals(reset)) {
        tree.setModel(new DefaultTreeModel(root));
      } else {
        TreeUtil.COMPARE_COUNTER.set(0);
        TreeUtil.SWAP_COUNTER.set(0);
        DefaultMutableTreeNode r = TreeUtil.deepCopyTree(root, (DefaultMutableTreeNode) root.clone());
        if (check.equals(sort1)) {
          TreeUtil.sortTree1(r);
        } else if (check.equals(sort2)) {
          TreeUtil.sortTree2(r);
        } else {
          TreeUtil.sortTree3(r);
        }
        swapCounter(check.getText());
        tree.setModel(new DefaultTreeModel(r));
      }
      TreeUtil.expandAll(tree);
    };
    ButtonGroup bg = new ButtonGroup();
    Stream.of(reset, sort1, sort2, sort3).forEach(check -> {
      box.add(check);
      bg.add(check);
      check.addActionListener(listener);
    });
    add(box, BorderLayout.SOUTH);

    JPanel p = new JPanel(new BorderLayout());
    p.setBorder(BorderFactory.createTitledBorder("Sort JTree"));
    p.add(new JScrollPane(tree));
    add(p);
    TreeUtil.expandAll(tree);
    setPreferredSize(new Dimension(320, 240));
  }

  private static void swapCounter(String title) {
    if (TreeUtil.SWAP_COUNTER.get() == 0) {
      int cc = TreeUtil.COMPARE_COUNTER.get();
      System.out.format("%-24s - compare: %3d, swap: ---%n", title, cc);
    } else {
      int cc = TreeUtil.COMPARE_COUNTER.get();
      int sc = TreeUtil.SWAP_COUNTER.get();
      System.out.format("%-24s - compare: %3d, swap: %3d%n", title, cc, sc);
    }
  }

  public static void main(String[] args) {
    EventQueue.invokeLater(MainPanel::createAndShowGui);
  }

  private static void createAndShowGui() {
    try {
      UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
    } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException ex) {
      ex.printStackTrace();
      Toolkit.getDefaultToolkit().beep();
    }
    JFrame frame = new JFrame("@title@");
    frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    frame.getContentPane().add(new MainPanel());
    frame.pack();
    frame.setLocationRelativeTo(null);
    frame.setVisible(true);
  }
}

final class TreeUtil {
  public static final AtomicInteger COMPARE_COUNTER = new AtomicInteger();
  public static final AtomicInteger SWAP_COUNTER = new AtomicInteger();

  // // JDK 1.7.0
  // private static TreeNodeComparator tnc = new TreeNodeComparator();
  // private static class TreeNodeComparator implements Comparator<DefaultMutableTreeNode>, java.io.Serializable {
  //   private static final long serialVersionUID = 1L;
  //   @Override public int compare(DefaultMutableTreeNode a, DefaultMutableTreeNode b) {
  //     COMPARE_COUNTER.getAndIncrement();
  //     if (a.isLeaf() && !b.isLeaf()) {
  //       return 1;
  //     } else if (!a.isLeaf() && b.isLeaf()) {
  //       return -1;
  //     } else {
  //       String sa = a.getUserObject().toString();
  //       String sb = b.getUserObject().toString();
  //       return sa.compareToIgnoreCase(sb);
  //     }
  //   }
  // }

  // JDK 1.8.0
  private static Comparator<DefaultMutableTreeNode> tnc = Comparator.comparing(DefaultMutableTreeNode::isLeaf)
      .thenComparing(n -> n.getUserObject().toString());

  private TreeUtil() {
    /* Singleton */
  }

  // // https://community.oracle.com/thread/1355435 How to sort jTree Nodes
  // public static void sortTree0(DefaultMutableTreeNode root) {
  //   for (int i = 0; i < root.getChildCount(); i++) {
  //     DefaultMutableTreeNode node = (DefaultMutableTreeNode) root.getChildAt(i);
  //     for (int j = 0; j < i; j++) {
  //       DefaultMutableTreeNode prevNode = (DefaultMutableTreeNode) root.getChildAt(j);
  //       COMPARE_COUNTER.getAndIncrement();
  //       if (tnc.compare(node, prevNode) < 0) {
  //         root.insert(node, j);
  //         root.insert(prevNode, i);
  //         SWAP_COUNTER.getAndIncrement();
  //         i--; // add
  //         break;
  //       }
  //     }
  //     if (!node.isLeaf()) {
  //       sortTree0(node);
  //     }
  //   }
  // }

  public static void sortTree1(DefaultMutableTreeNode root) {
    int n = root.getChildCount();
    for (int i = 0; i < n - 1; i++) {
      for (int j = n - 1; j > i; j--) {
        DefaultMutableTreeNode curNode = (DefaultMutableTreeNode) root.getChildAt(j);
        DefaultMutableTreeNode prevNode = (DefaultMutableTreeNode) root.getChildAt(j - 1);
        if (!prevNode.isLeaf()) {
          sortTree1(prevNode);
        }
        if (tnc.compare(prevNode, curNode) > 0) {
          SWAP_COUNTER.getAndIncrement();
          root.insert(curNode, j - 1);
          root.insert(prevNode, j);
        }
      }
    }
  }

  private static void sort2(DefaultMutableTreeNode parent) {
    int n = parent.getChildCount();
    for (int i = 0; i < n - 1; i++) {
      int min = i;
      for (int j = i + 1; j < n; j++) {
        if (tnc.compare((DefaultMutableTreeNode) parent.getChildAt(min),
            (DefaultMutableTreeNode) parent.getChildAt(j)) > 0) {
          min = j;
        }
      }
      if (i != min) {
        SWAP_COUNTER.getAndIncrement();
        MutableTreeNode a = (MutableTreeNode) parent.getChildAt(i);
        MutableTreeNode b = (MutableTreeNode) parent.getChildAt(min);
        parent.insert(b, i);
        parent.insert(a, min);
        // MutableTreeNode node = (MutableTreeNode) parent.getChildAt(min);
        // parent.insert(node, i);
        // COMPARE_COUNTER++;
      }
    }
  }

  public static void sortTree2(DefaultMutableTreeNode parent) {
    // Java 9: Collections.list(parent.preorderEnumeration()).stream()
    Collections.list((Enumeration<?>) parent.preorderEnumeration()).stream()
        .filter(DefaultMutableTreeNode.class::isInstance)
        .map(DefaultMutableTreeNode.class::cast)
        .filter(node -> !node.isLeaf())
        .forEach(TreeUtil::sort2);
  }

  private static void sort3(DefaultMutableTreeNode parent) {
    // @SuppressWarnings("unchecked")
    // Enumeration<DefaultMutableTreeNode> e = parent.children();
    // ArrayList<DefaultMutableTreeNode> children = Collections.list(e);

    int n = parent.getChildCount();
    List<DefaultMutableTreeNode> children = new ArrayList<>(n);
    for (int i = 0; i < n; i++) {
      children.add((DefaultMutableTreeNode) parent.getChildAt(i));
    }

    children.sort(tnc);
    parent.removeAllChildren();
    children.forEach(parent::add);
  }

  public static void sortTree3(DefaultMutableTreeNode parent) {
    // Java 9: Collections.list(parent.preorderEnumeration()).stream()
    Collections.list((Enumeration<?>) parent.preorderEnumeration()).stream()
        .filter(DefaultMutableTreeNode.class::isInstance)
        .map(DefaultMutableTreeNode.class::cast)
        .filter(node -> !node.isLeaf())
        .forEach(TreeUtil::sort3);
  }

  public static DefaultMutableTreeNode deepCopyTree(DefaultMutableTreeNode src, DefaultMutableTreeNode tgt) {
    // Java 9: Collections.list(src.children()).stream()
    Collections.list((Enumeration<?>) src.children()).stream()
        .filter(DefaultMutableTreeNode.class::isInstance)
        .map(DefaultMutableTreeNode.class::cast)
        .forEach(node -> {
          DefaultMutableTreeNode clone = new DefaultMutableTreeNode(node.getUserObject());
          tgt.add(clone);
          if (!node.isLeaf()) {
            deepCopyTree(node, clone);
          }
        });
    // for (int i = 0; i < src.getChildCount(); i++) {
    //   DefaultMutableTreeNode node = (DefaultMutableTreeNode) src.getChildAt(i);
    //   DefaultMutableTreeNode clone = new DefaultMutableTreeNode(node.getUserObject());
    //   // DefaultMutableTreeNode clone = (DefaultMutableTreeNode) node.clone();
    //   tgt.add(clone);
    //   if (!node.isLeaf()) {
    //     deepCopyTree(node, clone);
    //   }
    // }
    return tgt;
  }

  public static DefaultMutableTreeNode makeTreeRoot() {
    DefaultMutableTreeNode set1 = new DefaultMutableTreeNode("Set 001");
    DefaultMutableTreeNode set4 = new DefaultMutableTreeNode("Set 004");
    set1.add(new DefaultMutableTreeNode("3333333333333333"));
    set1.add(new DefaultMutableTreeNode("111111111"));
    set1.add(new DefaultMutableTreeNode("22222222222"));
    set1.add(set4);
    set1.add(new DefaultMutableTreeNode("222222"));
    set1.add(new DefaultMutableTreeNode("222222222"));

    DefaultMutableTreeNode set2 = new DefaultMutableTreeNode("Set 002");
    set2.add(new DefaultMutableTreeNode("eee eee eee eee e"));
    set2.add(new DefaultMutableTreeNode("bbb ccc aaa bbb"));

    DefaultMutableTreeNode set3 = new DefaultMutableTreeNode("Set 003");
    set3.add(new DefaultMutableTreeNode("zzz zz zz"));
    set3.add(new DefaultMutableTreeNode("aaa aaa aaa aaa"));
    set3.add(new DefaultMutableTreeNode("ccc ccc ccc"));

    set4.add(new DefaultMutableTreeNode("22222222222"));
    set4.add(new DefaultMutableTreeNode("eee eee eee ee ee"));
    set4.add(new DefaultMutableTreeNode("bbb bbb bbb bbb"));
    set4.add(new DefaultMutableTreeNode("zzz zz zz"));

    DefaultMutableTreeNode root = new DefaultMutableTreeNode("Root");
    root.add(new DefaultMutableTreeNode("xxx xxx xxx xx xx"));
    root.add(set3);
    root.add(new DefaultMutableTreeNode("eee eee eee ee ee"));
    root.add(set1);
    root.add(set2);
    root.add(new DefaultMutableTreeNode("222222222222"));
    root.add(new DefaultMutableTreeNode("bbb bbb bbb bbb"));
    return root;
  }

  public static void expandAll(JTree tree) {
    int row = 0;
    while (row < tree.getRowCount()) {
      tree.expandRow(row++);
    }
  }
}
