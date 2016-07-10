/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * SemanticMapping.java
 *
 * Created on 03.12.2010, 13:41:52
 */
package o_s;

import ClientServerPrototype.Client;
import WordBacklightPackage.ScannerTextHighlight;
import WordBacklightPackage.ScannerTextHighlightForTerminWithMultipleWords;
import com.mongodb.*;
import com.mongodb.gridfs.GridFS;
import com.sun.jna.Library;
import com.sun.jna.Native;
import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeModel;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import redis.clients.jedis.Jedis;

/**
 *
 * @author Человек
 */
public class SemanticMapping extends javax.swing.JFrame implements ActionListener {

    // Для контекстного меню tableConfor, обработка экшнов
    @Override
    public void actionPerformed(ActionEvent ae) {
        JMenuItem menu = (JMenuItem) ae.getSource();
        if (menu == menuItemCopySelectedCell) {
            copySelectedCellTableConfor();
        } else if (menu == menuItemPasteSelectedCell) {
            pasteSelectedCellTableConfor();
        } else if (menu == menuItemRemove) {
            removeCurrentRowTableConfor();
        } else if (menu == menuItemAdd) {
            addRowTableConfor();
        } else if (menu == menuItemAddColumn) {
            addColumnTableConfor();
        } else if (menu == menuItemRemoveAll) {
            removeAllRowsTableConfor();
        } else if (menu == menuItemSaveToFile) {
            try {
                toFileTableConfor(tableConfor, new File("tab.csv"));
            } catch (IOException ex) {
                Logger.getLogger(SemanticMapping.class.getName()).log(Level.SEVERE, null, ex);
            }
        } else if (menu == menuItemRemoveCurrentColumn) {
            removeCurrentColumnTableConfor();
        }




    }

    //Интерфейс и класс инициализации работы KonspektLib.dll
    public interface KonspektLib extends Library {

        public boolean ExportTerms(String filename, String termsfname, int TermsWordLevel, int TermsWordGroupr, boolean TermsFromCompleteSent, int TermsFreq);
    }

    public void TextAnalysis(String in_filename, String out_filename) {


        //KonspektLib lib = (KonspektLib) Native.loadLibrary(System.getProperty("user.dir")+"/KonspektLib.dll", KonspektLib.class);
        KonspektLib lib = (KonspektLib) Native.loadLibrary("KonspektLib.dll", KonspektLib.class);
        lib.ExportTerms(in_filename, out_filename, 99, 99, false, 0);

    }

    //Интерфейс и класс инициализации работы KonspektLib.dll
    /**
     * Creates new form SemanticMapping
     */
    public SemanticMapping() {
        initComponents();

        
        
        //
        //
        //Скрыть панель для фаворитных терминов
        FavTermsPanel.setVisible(false);


        this.pack();



        Port_IP = new Port_IP_Frame();
        this.setLocationRelativeTo(getRootPane());
        MainToolBar.setVisible(false);
        TermsTreePanel.setVisible(false);
        DeleteArchiveMenuItem.setVisible(false);
        ShowPrefWinMenuItem.setVisible(false);
        DeleteForLibReadButton.setVisible(false);
        jMenuItem2.setVisible(false);
        jMenuItem3.setVisible(false);
        jMenuItem4.setVisible(false);
        PropertiesMenu.setVisible(false);
        ShowSentencesInSeparateWinMenuItem.setVisible(false);
        DataBaseFile = new File("ArmIndDoc.DAT");
        CSVFileWrite = new File("write_doc.csv");
        CSVFileRead = new File("rrr_doc.csv");
        if (DataBaseFile.exists()) {

            // jTextPane1.setText("Архів термінів існує");
            StatusBarTextField.setText("Архив терминов существует");

        } else {
            //jTextPane1.setText("Архів термінів не існує");
            StatusBarTextField.setText("Архив терминов не существует");
        }
//        Dimension dimension = Toolkit.getDefaultToolkit().getScreenSize();
//        int x = dimension.width;
//        int y = dimension.height;
//        this.setSize(x, y);
//        this.setLocation(0, 0);

        this.setSize(1024, 768);
        this.setLocationRelativeTo(null);

// Maximize window       
        //this.setExtendedState(JFrame.MAXIMIZED_BOTH);
// Maximize window           
    }
    public static final String allTerms_inComboBox = "Всі терміни";
    public static final String Nouns = "Іменники";
    public static final String NounNoun = "Іменник+Іменник";
    public static final String NounNounNoun = "Іменник+Іменник+Іменник";
    public static final String AdjNoun = "Прикметник+Іменник";
    public static final String AdjAdjNoun = "Прикметник+Прикметник+Іменник";
    public static final String Abbr = "Абревіатура";
    public static final String _Noun = "Noun";
    //public static final String _AdjNoun = "AdjNoun";
    public static final String _AdjNoun = "Adj_noun";
    //public static final String _NounNoun = "NounNoun";
    public static final String _NounNoun = "Noun_noun";
    //public static final String _NounNounNoun = "NounNounNoun";
    public static final String _NounNounNoun = "Noun_noun_noun";
    //public static final String _AdjAdjNoun = "AdjAdjNoun";
    public static final String _AdjAdjNoun = "Adj_adj_noun";
    public static final String _Abbr = "Abbr";
    private static final String ENCODING_WIN1251 = "windows-1251";
    private static final String ENCODING_UTF8 = "UTF-8";
    private static final String TagSentence = "<sentences>";
    private static final String TagExporterms = "<exporterms>";
    private static final String KonspektLibFileOut = "termsexport.tt";
    private String SelectedFileCanonicalPath = null;
    private Document doc;
    public File CSVFileWrite;
    public File DataBaseFile;
    public File CSVFileRead;
    public File f_result;
    DefaultListModel listmodel_;
    DefaultListModel listmodel_1;
    DefaultListModel listmodel_2;
    ArrayList ArrayTermsForLibWrite;
    ArrayList ArrayTermsABC;
    ArrayList ArrayTermsWcount;
    ArrayList ArrayTermsNoun;
    ArrayList ArrayTermsAdjNoun;
    DefaultListModel listmodelNoun;
    DefaultListModel listmodelAdjNoun;
    ArrayList ArrayTermsNounNoun;
    DefaultListModel listmodelNounNoun;
    ArrayList ArrayTermsNounNounNoun;
    DefaultListModel listmodelNounNounNoun;
    DefaultListModel listmodel;
    ArrayList ArrayTermsAdjAdjNoun;
    DefaultListModel listmodelAdjAdjNoun;
    ArrayList ArrayTermsAbbr;
    DefaultListModel listmodelAbbr;
    ArrayList ArraySentences;
    ArrayList ArrayExportTerms;
    ArrayList ArrayListForSentOutInOneWindow;
    String TerminThatSelectedInJlist;
    String Stringjlist1;
    String StringPos;
    String WordPosInSring;
    FilteringJList list;
    JFrame frame;
    int i1;
    ArrayList ArrayListForSentecesOutput;
    String Log_OUT;
    backlight_word_in_JtextPane bw;
    BacklightWordInAllSentences bw1;
    String sbw = "text";
    int posOfBw = 10;
    int NomerSlovaVPredlogenii[];
    int NomerPredlogenia[];
    int ttt;
    Prefereces PreferecesWindow;
    DefaultListModel model_ = new DefaultListModel();
    ArrayList ArrayNomerPredlogenia;
    ArrayList ArrayNomerSlovaVPredlogenii;
    String DlyaNomeraSlovaVPredlogenii = new String();
    String DlyaNomeraPredlogeniya = new String();
    SortByWcount[] SBWcount;
    SortByDescending[] SBDescending;
    int CountAllTerms;
    int CountAllTerms_;
    JFileChooser FileChooserSave;
    TransferHandlerTermsFavListTermsList arrayListHandler = new TransferHandlerTermsFavListTermsList();
    BufferedReader br_libred;
    int NumberOfWordsInTermin;
    DefaultTreeModel TreeModel;
    public String IP_address = "";
    public String Port_address;
    Port_IP_Frame Port_IP;

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        FileChooserOpen = new javax.swing.JFileChooser();
        popupMenu = new javax.swing.JPopupMenu();
        SemanticMappingTabbedPane = new javax.swing.JTabbedPane();
        MainSemMapPanel = new javax.swing.JPanel();
        TermsPanel = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jComboBox1 = new javax.swing.JComboBox();
        jLabel2 = new javax.swing.JLabel();
        jScrollPane3 = new javax.swing.JScrollPane();
        TermsList = new javax.swing.JList();
        MultipleSelectShowSentencesButton = new javax.swing.JButton();
        FavTermsPanel = new javax.swing.JPanel();
        jScrollPane4 = new javax.swing.JScrollPane();
        TermsFavList = new javax.swing.JList();
        DeleteFavButton = new javax.swing.JButton();
        ShowFavButton = new javax.swing.JButton();
        SaveFavButton = new javax.swing.JButton();
        SelectFavButton = new javax.swing.JButton();
        ForShowSentencesPanel = new javax.swing.JPanel();
        jScrollPane5 = new javax.swing.JScrollPane();
        ShowSentencesTextPane = new javax.swing.JTextPane();
        TermsTreePanel = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        TermsTree = new javax.swing.JTree();
        StatusBarTextField = new javax.swing.JTextField();
        gridPanel = new javax.swing.JPanel();
        jScrollPane6 = new javax.swing.JScrollPane();
        tableConfor = new javax.swing.JTable();
        MongoDBPanel = new javax.swing.JPanel();
        jScrollPane7 = new javax.swing.JScrollPane();
        listMongoDB = new javax.swing.JList();
        buttonAllCollectionMongoBD = new javax.swing.JButton();
        buttonLigCorpusCollectionMongoBD = new javax.swing.JButton();
        buttonOntologiesCollectionMongoBD = new javax.swing.JButton();
        jScrollPane8 = new javax.swing.JScrollPane();
        jTextArea1 = new javax.swing.JTextArea();
        ArcSearchPanel = new javax.swing.JPanel();
        ForLibReadTextField = new javax.swing.JTextField();
        jLabel3 = new javax.swing.JLabel();
        LibReadButton = new javax.swing.JButton();
        jScrollPane2 = new javax.swing.JScrollPane();
        ForLibReadTextArea = new javax.swing.JTextArea();
        jLabel4 = new javax.swing.JLabel();
        DeleteForLibReadButton = new javax.swing.JButton();
        MainToolBar = new javax.swing.JToolBar();
        OpenFileButton = new javax.swing.JButton();
        SearchButton = new javax.swing.JButton();
        MultipleSelectionToggleButton = new javax.swing.JToggleButton();
        LibWriteButton = new javax.swing.JButton();
        DeleteArmSpeedArchiveButton = new javax.swing.JButton();
        MainMenuBar = new javax.swing.JMenuBar();
        FileMenu = new javax.swing.JMenu();
        OpenFileMenuItem = new javax.swing.JMenuItem();
        MenuLibWriteMenuItem = new javax.swing.JMenuItem();
        DeleteArchiveMenuItem = new javax.swing.JMenuItem();
        SortMenuItem = new javax.swing.JMenu();
        alphabeticallyMenuItem = new javax.swing.JMenuItem();
        WcountMenuItem = new javax.swing.JMenuItem();
        Wcount1MenuItem = new javax.swing.JMenuItem();
        FindMenuItem = new javax.swing.JMenuItem();
        PropertiesMenu = new javax.swing.JMenu();
        jMenuItem2 = new javax.swing.JMenuItem();
        jMenuItem3 = new javax.swing.JMenuItem();
        jMenuItem4 = new javax.swing.JMenuItem();
        PreferencesMenu = new javax.swing.JMenu();
        ShowSentencesInSeparateWinMenuItem = new javax.swing.JCheckBoxMenuItem();
        ShowSentWinSeparateCheckBoxMenuItem = new javax.swing.JCheckBoxMenuItem();
        AdditionWinCheckBoxMenuItem = new javax.swing.JCheckBoxMenuItem();
        MultipleSelectionCheckBoxMenuItem = new javax.swing.JCheckBoxMenuItem();
        AdditionToolsMenuItem = new javax.swing.JMenuItem();
        TermsTreeCheckBoxMenuItem = new javax.swing.JCheckBoxMenuItem();
        KonspektLibRunCheckBoxMenuItem = new javax.swing.JCheckBoxMenuItem();
        jMenuItem5 = new javax.swing.JMenuItem();
        RedisCheckBoxMenuItem = new javax.swing.JCheckBoxMenuItem();
        ClientServerMode = new javax.swing.JCheckBoxMenuItem();
        jMenuItem6 = new javax.swing.JMenuItem();
        ShowPrefWinMenuItem = new javax.swing.JMenuItem();
        CloseModuleMenuItem = new javax.swing.JMenuItem();
        jMenu1 = new javax.swing.JMenu();
        jMenuItem1 = new javax.swing.JMenuItem();
        jMenu2 = new javax.swing.JMenu();

        FileChooserOpen.setFileFilter(new TXTFileFilter());
        FileChooserOpen.setFileFilter(new TTFileFilter());
        FileChooserOpen.setFileFilter(new o_s.XmlFileFilterCorrect());

        menuItemAdd = new JMenuItem("Добавить ряд");
        menuItemAddColumn = new JMenuItem("Добавить колонку");
        menuItemRemove = new JMenuItem("Удалить текущий ряд");
        menuItemRemoveCurrentColumn = new JMenuItem("Удалить текущую колонку");
        menuItemRemoveAll = new JMenuItem("Удалить все ряды");
        menuItemCopySelectedCell = new JMenuItem("Копировать ячейку");
        menuItemPasteSelectedCell = new JMenuItem("Вставить в ячейку");
        menuItemSaveToFile = new JMenuItem("Сохранить в файл");

        popupMenu.add(menuItemAdd);
        popupMenu.add(menuItemAddColumn);
        popupMenu.add(menuItemRemove);
        popupMenu.add(menuItemRemoveCurrentColumn);
        popupMenu.add(menuItemRemoveAll);
        popupMenu.add(menuItemCopySelectedCell);
        popupMenu.add(menuItemPasteSelectedCell);
        popupMenu.add(menuItemSaveToFile);

        menuItemCopySelectedCell.addActionListener(this);
        menuItemPasteSelectedCell.addActionListener(this);
        menuItemRemove.addActionListener(this);
        menuItemAdd.addActionListener(this);
        menuItemAddColumn.addActionListener(this);
        menuItemRemoveAll.addActionListener(this);
        menuItemSaveToFile.addActionListener(this);
        menuItemRemoveCurrentColumn.addActionListener(this);

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Модуль лингвистического анализа ТД");
        setName("MainFrame"); // NOI18N

        TermsPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Термины"));

        jLabel1.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel1.setText("Выберите тип терминов");

        jComboBox1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jComboBox1ActionPerformed(evt);
            }
        });

        jLabel2.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel2.setText("Выберите термин");

        TermsList.setDragEnabled(true);
        TermsList.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                TermsListMouseClicked(evt);
            }
        });
        TermsList.addListSelectionListener(new javax.swing.event.ListSelectionListener() {
            public void valueChanged(javax.swing.event.ListSelectionEvent evt) {
                TermsListValueChanged(evt);
            }
        });

        jScrollPane3.setViewportView(TermsList);

        MultipleSelectShowSentencesButton.setText("Отобразить предложения");
        MultipleSelectShowSentencesButton.setEnabled(false);
        MultipleSelectShowSentencesButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                MultipleSelectShowSentencesButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout TermsPanelLayout = new javax.swing.GroupLayout(TermsPanel);
        TermsPanel.setLayout(TermsPanelLayout);
        TermsPanelLayout.setHorizontalGroup(
            TermsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jLabel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(jComboBox1, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(jLabel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(MultipleSelectShowSentencesButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(jScrollPane3)
        );
        TermsPanelLayout.setVerticalGroup(
            TermsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(TermsPanelLayout.createSequentialGroup()
                .addComponent(jLabel1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jComboBox1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel2)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jScrollPane3)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(MultipleSelectShowSentencesButton))
        );

        FavTermsPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Избранное"));

        TermsFavList.setDoubleBuffered(true);
        TermsFavList.setDragEnabled(true);
        jScrollPane4.setViewportView(TermsFavList);

        DeleteFavButton.setText("Удалить");
        DeleteFavButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                DeleteFavButtonActionPerformed(evt);
            }
        });

        ShowFavButton.setText("Отобразить предложения");
        ShowFavButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ShowFavButtonActionPerformed(evt);
            }
        });

        SaveFavButton.setText("Сохранить");
        SaveFavButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                SaveFavButtonActionPerformed(evt);
            }
        });

        SelectFavButton.setText("Выбрать");
        SelectFavButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                SelectFavButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout FavTermsPanelLayout = new javax.swing.GroupLayout(FavTermsPanel);
        FavTermsPanel.setLayout(FavTermsPanelLayout);
        FavTermsPanelLayout.setHorizontalGroup(
            FavTermsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(ShowFavButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(jScrollPane4, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, FavTermsPanelLayout.createSequentialGroup()
                .addComponent(SelectFavButton)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(FavTermsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(DeleteFavButton, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(SaveFavButton, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
        );
        FavTermsPanelLayout.setVerticalGroup(
            FavTermsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(FavTermsPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(DeleteFavButton)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(FavTermsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(SaveFavButton)
                    .addComponent(SelectFavButton))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jScrollPane4)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(ShowFavButton))
        );

        jScrollPane5.setViewportView(ShowSentencesTextPane);

        javax.swing.GroupLayout ForShowSentencesPanelLayout = new javax.swing.GroupLayout(ForShowSentencesPanel);
        ForShowSentencesPanel.setLayout(ForShowSentencesPanelLayout);
        ForShowSentencesPanelLayout.setHorizontalGroup(
            ForShowSentencesPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane5)
        );
        ForShowSentencesPanelLayout.setVerticalGroup(
            ForShowSentencesPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane5, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 146, Short.MAX_VALUE)
        );

        TermsTreePanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Дерево терминов"));

        javax.swing.tree.DefaultMutableTreeNode treeNode1 = new javax.swing.tree.DefaultMutableTreeNode("Top");
        TermsTree.setModel(new javax.swing.tree.DefaultTreeModel(treeNode1));
        TermsTree.setDragEnabled(true);
        jScrollPane1.setViewportView(TermsTree);

        javax.swing.GroupLayout TermsTreePanelLayout = new javax.swing.GroupLayout(TermsTreePanel);
        TermsTreePanel.setLayout(TermsTreePanelLayout);
        TermsTreePanelLayout.setHorizontalGroup(
            TermsTreePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 246, Short.MAX_VALUE)
        );
        TermsTreePanelLayout.setVerticalGroup(
            TermsTreePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane1, javax.swing.GroupLayout.Alignment.TRAILING)
        );

        StatusBarTextField.setBackground(new java.awt.Color(153, 153, 153));
        StatusBarTextField.setEditable(false);
        StatusBarTextField.setBorder(null);

        gridPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Грид"));

        tableConfor.setModel(tableModel = new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null}
            },
            new String [] {
                "Title 1", "Title 2", "Title 3", "Title 4"
            }
        ));
        tableConfor.setAutoResizeMode(javax.swing.JTable.AUTO_RESIZE_OFF);
        tableConfor.setCellSelectionEnabled(true);
        tableConfor.setDragEnabled(false);
        tableConfor.setRequestFocusEnabled(false);
        tableConfor.setShowVerticalLines(true);
        tableConfor.setShowHorizontalLines(true);
        tableConfor.setTableHeader(null);
        tableConfor.getColumnModel().getSelectionModel().setSelectionMode(javax.swing.ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        tableConfor.setTransferHandler(new TransferHandlerForTableConfor());
        tableConfor.setComponentPopupMenu(popupMenu);
        tableConfor.addMouseListener(new MouseListenerTableConfor(tableConfor));
        tableConfor.setShowGrid(true);
        jScrollPane6.setViewportView(tableConfor);

        javax.swing.GroupLayout gridPanelLayout = new javax.swing.GroupLayout(gridPanel);
        gridPanel.setLayout(gridPanelLayout);
        gridPanelLayout.setHorizontalGroup(
            gridPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane6, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 474, Short.MAX_VALUE)
        );
        gridPanelLayout.setVerticalGroup(
            gridPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane6, javax.swing.GroupLayout.DEFAULT_SIZE, 414, Short.MAX_VALUE)
        );

        javax.swing.GroupLayout MainSemMapPanelLayout = new javax.swing.GroupLayout(MainSemMapPanel);
        MainSemMapPanel.setLayout(MainSemMapPanelLayout);
        MainSemMapPanelLayout.setHorizontalGroup(
            MainSemMapPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(MainSemMapPanelLayout.createSequentialGroup()
                .addComponent(TermsTreePanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(TermsPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(gridPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(FavTermsPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            .addComponent(StatusBarTextField)
            .addComponent(ForShowSentencesPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        MainSemMapPanelLayout.setVerticalGroup(
            MainSemMapPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, MainSemMapPanelLayout.createSequentialGroup()
                .addGroup(MainSemMapPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(TermsPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(FavTermsPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(TermsTreePanel, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(gridPanel, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(ForShowSentencesPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(StatusBarTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        SemanticMappingTabbedPane.addTab("Разбора текста", MainSemMapPanel);

        listMongoDB.setModel(listModelMongoDB = new DefaultListModel());
        jScrollPane7.setViewportView(listMongoDB);

        buttonAllCollectionMongoBD.setText("Все коллекции");
        buttonAllCollectionMongoBD.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonAllCollectionMongoBDActionPerformed(evt);
            }
        });

        buttonLigCorpusCollectionMongoBD.setText("Все файлы в коллекции ЛКТ");
        buttonLigCorpusCollectionMongoBD.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonLigCorpusCollectionMongoBDActionPerformed(evt);
            }
        });

        buttonOntologiesCollectionMongoBD.setText("Все файлы в коллекции Онтологии");
        buttonOntologiesCollectionMongoBD.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonOntologiesCollectionMongoBDActionPerformed(evt);
            }
        });

        jTextArea1.setColumns(20);
        jTextArea1.setRows(5);
        jScrollPane8.setViewportView(jTextArea1);

        javax.swing.GroupLayout MongoDBPanelLayout = new javax.swing.GroupLayout(MongoDBPanel);
        MongoDBPanel.setLayout(MongoDBPanelLayout);
        MongoDBPanelLayout.setHorizontalGroup(
            MongoDBPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(MongoDBPanelLayout.createSequentialGroup()
                .addComponent(jScrollPane7, javax.swing.GroupLayout.PREFERRED_SIZE, 309, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGroup(MongoDBPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(MongoDBPanelLayout.createSequentialGroup()
                        .addGap(229, 229, 229)
                        .addGroup(MongoDBPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(buttonOntologiesCollectionMongoBD)
                            .addComponent(buttonLigCorpusCollectionMongoBD)
                            .addComponent(buttonAllCollectionMongoBD))
                        .addGap(0, 433, Short.MAX_VALUE))
                    .addGroup(MongoDBPanelLayout.createSequentialGroup()
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jScrollPane8)
                        .addContainerGap())))
        );
        MongoDBPanelLayout.setVerticalGroup(
            MongoDBPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane7, javax.swing.GroupLayout.DEFAULT_SIZE, 616, Short.MAX_VALUE)
            .addGroup(MongoDBPanelLayout.createSequentialGroup()
                .addComponent(buttonAllCollectionMongoBD)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(buttonLigCorpusCollectionMongoBD)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(buttonOntologiesCollectionMongoBD)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jScrollPane8, javax.swing.GroupLayout.PREFERRED_SIZE, 245, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        SemanticMappingTabbedPane.addTab("Работа с базой данных", MongoDBPanel);

        jLabel3.setText("Ключевое слово");

        LibReadButton.setText("Поиск");
        LibReadButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                LibReadButtonActionPerformed(evt);
            }
        });

        ForLibReadTextArea.setColumns(20);
        ForLibReadTextArea.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        ForLibReadTextArea.setRows(5);
        jScrollPane2.setViewportView(ForLibReadTextArea);

        jLabel4.setText("Результат поиска");

        DeleteForLibReadButton.setText("Удалить архив");
        DeleteForLibReadButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                DeleteForLibReadButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout ArcSearchPanelLayout = new javax.swing.GroupLayout(ArcSearchPanel);
        ArcSearchPanel.setLayout(ArcSearchPanelLayout);
        ArcSearchPanelLayout.setHorizontalGroup(
            ArcSearchPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(ForLibReadTextField, javax.swing.GroupLayout.DEFAULT_SIZE, 1261, Short.MAX_VALUE)
            .addGroup(ArcSearchPanelLayout.createSequentialGroup()
                .addGroup(ArcSearchPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 1235, Short.MAX_VALUE)
                    .addGroup(ArcSearchPanelLayout.createSequentialGroup()
                        .addGroup(ArcSearchPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel3)
                            .addComponent(LibReadButton, javax.swing.GroupLayout.PREFERRED_SIZE, 154, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel4)
                            .addComponent(DeleteForLibReadButton, javax.swing.GroupLayout.PREFERRED_SIZE, 158, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );
        ArcSearchPanelLayout.setVerticalGroup(
            ArcSearchPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(ArcSearchPanelLayout.createSequentialGroup()
                .addGap(62, 62, 62)
                .addComponent(jLabel3)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(ForLibReadTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(LibReadButton)
                .addGap(28, 28, 28)
                .addComponent(jLabel4)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 355, Short.MAX_VALUE)
                .addGap(18, 18, 18)
                .addComponent(DeleteForLibReadButton)
                .addGap(17, 17, 17))
        );

        SemanticMappingTabbedPane.addTab("Архив терминов", ArcSearchPanel);

        MainToolBar.setRollover(true);

        OpenFileButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/o_s/images/Open_48x48x32.png"))); // NOI18N
        OpenFileButton.setToolTipText("Відкрити текст");
        OpenFileButton.setBorderPainted(false);
        OpenFileButton.setFocusable(false);
        OpenFileButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        OpenFileButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        OpenFileButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                OpenFileMenuItemActionPerformed(evt);
            }
        });
        MainToolBar.add(OpenFileButton);

        SearchButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/o_s/images/Searches_48x48x32.png"))); // NOI18N
        SearchButton.setToolTipText("Пошук");
        SearchButton.setBorderPainted(false);
        SearchButton.setEnabled(false);
        SearchButton.setFocusable(false);
        SearchButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        SearchButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        SearchButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                FindMenuItemActionPerformed(evt);
            }
        });
        MainToolBar.add(SearchButton);

        MultipleSelectionToggleButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/o_s/images/Groups_48x48x32.png"))); // NOI18N
        MultipleSelectionToggleButton.setToolTipText("Множинный вибір");
        MultipleSelectionToggleButton.setFocusable(false);
        MultipleSelectionToggleButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        MultipleSelectionToggleButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        MultipleSelectionToggleButton.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                MultipleSelectionToggleButtonStateChanged(evt);
            }
        });
        MultipleSelectionToggleButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                MultipleSelectionToggleButtonActionPerformed(evt);
            }
        });
        MainToolBar.add(MultipleSelectionToggleButton);

        LibWriteButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/o_s/images/Downloads_48x48x32.png"))); // NOI18N
        LibWriteButton.setToolTipText("Зберегти терміни в архів");
        LibWriteButton.setBorderPainted(false);
        LibWriteButton.setFocusable(false);
        LibWriteButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        LibWriteButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        LibWriteButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                LibWriteButtonActionPerformed(evt);
            }
        });
        MainToolBar.add(LibWriteButton);

        DeleteArmSpeedArchiveButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/o_s/images/System_48x48x32.png"))); // NOI18N
        DeleteArmSpeedArchiveButton.setToolTipText("Видалити архів термінів");
        DeleteArmSpeedArchiveButton.setBorderPainted(false);
        DeleteArmSpeedArchiveButton.setFocusable(false);
        DeleteArmSpeedArchiveButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        DeleteArmSpeedArchiveButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        DeleteArmSpeedArchiveButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                DeleteArmSpeedArchiveButtonActionPerformed(evt);
            }
        });
        MainToolBar.add(DeleteArmSpeedArchiveButton);

        FileMenu.setText("Файл");

        OpenFileMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_O, java.awt.event.InputEvent.CTRL_MASK));
        OpenFileMenuItem.setText("Открыть файл для разбора");
        OpenFileMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                OpenFileMenuItemActionPerformed(evt);
            }
        });
        FileMenu.add(OpenFileMenuItem);

        MenuLibWriteMenuItem.setText("Сохранить в архив");
        MenuLibWriteMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                MenuLibWriteMenuItemActionPerformed(evt);
            }
        });
        FileMenu.add(MenuLibWriteMenuItem);

        DeleteArchiveMenuItem.setText("Удалить архив");
        DeleteArchiveMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                DeleteArchiveMenuItemActionPerformed(evt);
            }
        });
        FileMenu.add(DeleteArchiveMenuItem);

        SortMenuItem.setText("Сортировка");

        alphabeticallyMenuItem.setText("по алфавиту");
        alphabeticallyMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                alphabeticallyMenuItemActionPerformed(evt);
            }
        });
        SortMenuItem.add(alphabeticallyMenuItem);

        WcountMenuItem.setText("по частоте встречаемости (по увеличению)");
        WcountMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                WcountMenuItemActionPerformed(evt);
            }
        });
        SortMenuItem.add(WcountMenuItem);

        Wcount1MenuItem.setText("по частоте встречаемости (по уменьшению)");
        Wcount1MenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                Wcount1MenuItemActionPerformed(evt);
            }
        });
        SortMenuItem.add(Wcount1MenuItem);

        FileMenu.add(SortMenuItem);

        FindMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_F, java.awt.event.InputEvent.CTRL_MASK));
        FindMenuItem.setText("Поиск");
        FindMenuItem.setEnabled(false);
        FindMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                FindMenuItemActionPerformed(evt);
            }
        });
        FileMenu.add(FindMenuItem);

        PropertiesMenu.setText("Параметры отображения предложений из текста");

        jMenuItem2.setText("Стандартне вікно");
        jMenuItem2.setEnabled(false);
        jMenuItem2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem2ActionPerformed(evt);
            }
        });
        PropertiesMenu.add(jMenuItem2);

        jMenuItem3.setText("Демо вікно");
        jMenuItem3.setEnabled(false);
        jMenuItem3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem3ActionPerformed(evt);
            }
        });
        PropertiesMenu.add(jMenuItem3);

        jMenuItem4.setText("Закрити всі вікна з підсвіткою");
        jMenuItem4.setEnabled(false);
        jMenuItem4.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem4ActionPerformed(evt);
            }
        });
        PropertiesMenu.add(jMenuItem4);

        FileMenu.add(PropertiesMenu);

        PreferencesMenu.setText("Настройки");

        ShowSentencesInSeparateWinMenuItem.setText("Отображение каждого предложения в отдельном окне");
        ShowSentencesInSeparateWinMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ShowSentencesInSeparateWinMenuItemActionPerformed(evt);
            }
        });
        PreferencesMenu.add(ShowSentencesInSeparateWinMenuItem);

        ShowSentWinSeparateCheckBoxMenuItem.setText("Отображение предложений в отдельном окне");
        PreferencesMenu.add(ShowSentWinSeparateCheckBoxMenuItem);

        AdditionWinCheckBoxMenuItem.setSelected(true);
        AdditionWinCheckBoxMenuItem.setText("Дополнительное окно");
        AdditionWinCheckBoxMenuItem.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                AdditionWinCheckBoxMenuItemStateChanged(evt);
            }
        });
        PreferencesMenu.add(AdditionWinCheckBoxMenuItem);

        MultipleSelectionCheckBoxMenuItem.setText("Включить множественный выбор");
        MultipleSelectionCheckBoxMenuItem.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                MultipleSelectionCheckBoxMenuItemStateChanged(evt);
            }
        });
        MultipleSelectionCheckBoxMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                MultipleSelectionCheckBoxMenuItemActionPerformed(evt);
            }
        });
        PreferencesMenu.add(MultipleSelectionCheckBoxMenuItem);

        AdditionToolsMenuItem.setText("Включить дополнительные инструменты");
        AdditionToolsMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                AdditionToolsMenuItemActionPerformed(evt);
            }
        });
        PreferencesMenu.add(AdditionToolsMenuItem);

        TermsTreeCheckBoxMenuItem.setText("Отобразить дерево терминов");
        TermsTreeCheckBoxMenuItem.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                TermsTreeCheckBoxMenuItemStateChanged(evt);
            }
        });
        TermsTreeCheckBoxMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                TermsTreeCheckBoxMenuItemActionPerformed(evt);
            }
        });
        PreferencesMenu.add(TermsTreeCheckBoxMenuItem);

        KonspektLibRunCheckBoxMenuItem.setText("Работа c реальным тестом");
        PreferencesMenu.add(KonspektLibRunCheckBoxMenuItem);

        jMenuItem5.setText("Включить Избранное");
        jMenuItem5.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem5ActionPerformed(evt);
            }
        });
        PreferencesMenu.add(jMenuItem5);

        RedisCheckBoxMenuItem.setText("Работа в режиме клиента Redis");
        RedisCheckBoxMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                RedisCheckBoxMenuItemActionPerformed(evt);
            }
        });
        PreferencesMenu.add(RedisCheckBoxMenuItem);

        ClientServerMode.setText("Работа в режиме клиент-сервер");
        PreferencesMenu.add(ClientServerMode);

        jMenuItem6.setText("Конфигурация Port/IP");
        jMenuItem6.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem6ActionPerformed(evt);
            }
        });
        PreferencesMenu.add(jMenuItem6);

        FileMenu.add(PreferencesMenu);

        ShowPrefWinMenuItem.setText("Параметры");
        ShowPrefWinMenuItem.setEnabled(false);
        ShowPrefWinMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ShowPrefWinMenuItemActionPerformed(evt);
            }
        });
        FileMenu.add(ShowPrefWinMenuItem);

        CloseModuleMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_ESCAPE, java.awt.event.InputEvent.CTRL_MASK));
        CloseModuleMenuItem.setText("Закрыть модуль");
        CloseModuleMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                CloseModuleMenuItemActionPerformed(evt);
            }
        });
        FileMenu.add(CloseModuleMenuItem);

        MainMenuBar.add(FileMenu);

        jMenu1.setText("Редактировать");

        jMenuItem1.setText("Сохранить дерево терминов");
        jMenuItem1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem1ActionPerformed(evt);
            }
        });
        jMenu1.add(jMenuItem1);

        MainMenuBar.add(jMenu1);

        jMenu2.setText("Помощь");
        MainMenuBar.add(jMenu2);

        setJMenuBar(MainMenuBar);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(MainToolBar, javax.swing.GroupLayout.DEFAULT_SIZE, 1274, Short.MAX_VALUE)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(SemanticMappingTabbedPane, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(MainToolBar, javax.swing.GroupLayout.PREFERRED_SIZE, 43, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(SemanticMappingTabbedPane)
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents
    //////////////////////////////////////  
    //для работы в режиме клиент-сервер   
    /////////////////////////////////////// 
    Socket s = null;
    OutputStream out;

    public void sendBytes(byte[] myByteArray, int start, int len) throws IOException {
        if (len < 0) {
            throw new IllegalArgumentException("Negative length not allowed");
        }
        if (start < 0 || start >= myByteArray.length) {
            throw new IndexOutOfBoundsException("Out of bounds: " + start);
        }
        // Other checks if needed.

        // May be better to save the streams in the support class;
        // just like the socket variable.

        try {


            //////////////////////////////////////  
            //для работы в режиме клиент-сервер необходимо задать ip адрес ПК на котором запущена серверная
            //часть ИКОН
            ///////////////////////////////////////


            s = new Socket(Port_IP.GetIP(), Integer.parseInt(Port_IP.GetPort()));

            // Integer.parseInt(args[0]), Integer.parseInt(args[1])
            //s = new Socket("100.100.100.5", 8189);
        } catch (UnknownHostException ex) {
            Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
        }


        OutputStream out = s.getOutputStream();
        DataOutputStream dos = new DataOutputStream(out);

        dos.writeInt(len);
        if (len > 0) {
            dos.write(myByteArray, start, len);
        }
    }

    public void sendBytes(byte[] myByteArray) throws IOException {
        sendBytes(myByteArray, 0, myByteArray.length);
    }

    // Get bytes from stream. Get file in byte array from stream
    public byte[] readBytes() throws IOException {
        // Again, probably better to store these objects references in the support class
        InputStream in = s.getInputStream();
        DataInputStream dis = new DataInputStream(in);

        int len = dis.readInt();
        //byte[] data = new byte[dis.available()];
        byte[] data = new byte[len];
        if (len > 0) {
            dis.readFully(data);
        }
        return data;
    }
    // Get bytes from stream. Get file in byte array from stream

    //////////////////////////////////////  
    //для работы в режиме клиент-сервер   
    ///////////////////////////////////////   
    public void AutomateIntegratedTechnology(String SelectedFile) {

        CountAllTerms = 0;
        int CountNoun = 0;
        int CountAdjNoun = 0;
        int CountNounNoun = 0;
        int CountNounNounNoun = 0;
        int CountAdjAdjNoun = 0;
        int CountAbbr = 0;



        ArrayExportTerms = new ArrayList<String>();
        ArraySentences = new ArrayList<String>();
        ArrayTermsForLibWrite = new ArrayList<String>();
        ArrayTermsABC = new ArrayList<String>();
        ArrayTermsWcount = new ArrayList<String>();
        ArrayTermsNoun = new ArrayList<String>();
        listmodelNoun = new DefaultListModel();
        ArrayTermsAdjNoun = new ArrayList<String>();
        listmodelAdjNoun = new DefaultListModel();
        ArrayTermsNounNoun = new ArrayList<String>();
        listmodelNounNoun = new DefaultListModel();
        ArrayTermsNounNounNoun = new ArrayList<String>();
        listmodelNounNounNoun = new DefaultListModel();
        listmodelAdjAdjNoun = new DefaultListModel();
        ArrayTermsAdjAdjNoun = new ArrayList<String>();
        listmodelAbbr = new DefaultListModel();
        ArrayTermsAbbr = new ArrayList<String>();
        ArrayListForSentecesOutput = new ArrayList<String>();
        ArrayListForSentOutInOneWindow = new ArrayList<String>();
        //SBWcount = new SortByWcount[107];
        SBWcount = new SortByWcount[3000];
        SBDescending = new SortByDescending[3000];


        //Хэндлеры для списков JList
        if (FavTermsPanel.isVisible()) {
            TermsList.setTransferHandler(arrayListHandler);
            TermsFavList.setTransferHandler(arrayListHandler);
        } else {
            TermsList.setTransferHandler(null);
            TermsFavList.setTransferHandler(null);

        }


        TermsList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        TermsFavList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);


        TermsFavList.setModel(model_);

        File KospektDll = new File("KonspektLib.dll");


        System.out.println("konspekt.dll run with real text file from Main_CGS");


        BufferedReader br = null;
        String S_sentences = null;



        System.out.println("+ работа dll библиотеки");

        ////KonspektLibFileOut - "termsexport.tt" имя файла результата
        ////разбора по умолчанию
        ////
        TextAnalysis(SelectedFile, KonspektLibFileOut);


        try {
            try {
                br = new BufferedReader(new InputStreamReader(new FileInputStream(KonspektLibFileOut), ENCODING_WIN1251));
            } catch (UnsupportedEncodingException ex) {
                Logger.getLogger(SemanticMapping.class.getName()).log(Level.SEVERE, null, ex);
                System.out.println("Unsupported Encoding!");
            }

        } catch (FileNotFoundException ex) {
            Logger.getLogger(SemanticMapping.class.getName()).log(Level.SEVERE, null, ex);
            System.out.println("File Not Found!");
        }
        try {
            while ((S_sentences = br.readLine()) != null) {

                ArraySentences.add(S_sentences);
                ArrayExportTerms.add(S_sentences);
            }
        } catch (IOException ex) {
            Logger.getLogger(SemanticMapping.class.getName()).log(Level.SEVERE, null, ex);
        }


        while (TagSentence.equals(ArraySentences.get(0)) != true) {
            ArraySentences.remove(0);
        }
        ArraySentences.remove(0);


        DefaultComboBoxModel comboboxModel1 = new DefaultComboBoxModel();
        listmodel = new DefaultListModel();



        doc = new Document();


        // List namedChildren = rootElement.getChildren("term");
        SAXBuilder sb = new SAXBuilder();
        try {
            // помещение в память считываемого файла xml
            doc = sb.build(new File(KonspektLibFileOut));
        } catch (JDOMException ex) {
        } catch (IOException ex) {
        }
        Element rootElement = doc.getRootElement();
        Element exportTerms = rootElement.getChild("exporterms");
        Element sentences = rootElement.getChild("sentences");
        List terms = exportTerms.getChildren("term");
        java.util.Iterator i = terms.iterator();






        while (i.hasNext()) {


            Element term = (Element) i.next();
            System.out.println(term.getChild("tname").getText()
                    + " wcount=" + term.getChild("wcount").getText()
                    + " sentposSize=" + term.getChildren("sentpos").size());
            listmodel.addElement(term.getChild("tname").getText() + "(" + term.getChildren("sentpos").size() + ")");

            ArrayTermsABC.add(term.getChild("tname").getText() + "(" + term.getChildren("sentpos").size() + ")");
            ArrayTermsForLibWrite.add(term.getChild("tname").getText());


            ArrayTermsWcount.add(term.getChildren("sentpos").size() + " " + term.getChild("tname").getText().toLowerCase());



            SBWcount[CountAllTerms] = new SortByWcount(term.getChildren("sentpos").size(), term.getChild("tname").getText().toLowerCase());
            SBDescending[CountAllTerms] = new SortByDescending(term.getChildren("sentpos").size(), term.getChild("tname").getText().toLowerCase());


            CountAllTerms++;


            if (_Noun.equals(term.getChild("ttype").getText())) {
                ArrayTermsNoun.add(term.getChild("tname").getText() + "(" + term.getChildren("sentpos").size() + ")");
                listmodelNoun.addElement(term.getChild("tname").getText() + "(" + term.getChildren("sentpos").size() + ")");
                CountNoun++;

            }

            if (_AdjNoun.equals(term.getChild("ttype").getText())) {
                ArrayTermsAdjNoun.add(term.getChild("tname").getText() + "(" + term.getChildren("sentpos").size() + ")");
                listmodelAdjNoun.addElement(term.getChild("tname").getText() + "(" + term.getChildren("sentpos").size() + ")");
                CountAdjNoun++;
            }

            if (_NounNoun.equals(term.getChild("ttype").getText())) {
                ArrayTermsNounNoun.add(term.getChild("tname").getText() + "(" + term.getChildren("sentpos").size() + ")");
                listmodelNounNoun.addElement(term.getChild("tname").getText() + "(" + term.getChildren("sentpos").size() + ")");
                CountNounNoun++;
            }

            if (_NounNounNoun.equals(term.getChild("ttype").getText())) {
                ArrayTermsNounNounNoun.add(term.getChild("tname").getText() + "(" + term.getChildren("sentpos").size() + ")");
                listmodelNounNounNoun.addElement(term.getChild("tname").getText() + "(" + term.getChildren("sentpos").size() + ")");
                CountNounNounNoun++;
            }

            if (_AdjAdjNoun.equals(term.getChild("ttype").getText())) {
                ArrayTermsAdjAdjNoun.add(term.getChild("tname").getText() + "(" + term.getChildren("sentpos").size() + ")");
                listmodelAdjAdjNoun.addElement(term.getChild("tname").getText() + "(" + term.getChildren("sentpos").size() + ")");
                CountAdjAdjNoun++;
            }

            if (_Abbr.equals(term.getChild("ttype").getText())) {
                ArrayTermsAbbr.add(term.getChild("tname").getText() + "(" + term.getChildren("sentpos").size() + ")");
                listmodelAbbr.addElement(term.getChild("tname").getText() + "(" + term.getChildren("sentpos").size() + ")");
                CountAbbr++;
            }


        }



        for (int ii = 0; ii < ArraySentences.size(); ii++) {

            System.out.println(ArraySentences.get(ii));

        }



//Поиск слова в строке


        comboboxModel1.addElement(allTerms_inComboBox + " (" + CountAllTerms + ")");
        comboboxModel1.addElement(Nouns + " (" + CountNoun + ")");
        comboboxModel1.addElement(AdjNoun + " (" + CountAdjNoun + ")");
        comboboxModel1.addElement(NounNoun + " (" + CountNounNoun + ")");
        comboboxModel1.addElement(NounNounNoun + " (" + CountNounNounNoun + ")");
        comboboxModel1.addElement(AdjAdjNoun + " (" + CountAdjAdjNoun + ")");
        comboboxModel1.addElement(Abbr + " (" + CountAbbr + ")");

        jComboBox1.setModel(comboboxModel1);
        TermsList.setModel(listmodel);
        FindMenuItem.setEnabled(true);
        SearchButton.setEnabled(true);


        try {
            br.close();
        } catch (IOException ex) {
            Logger.getLogger(SemanticMapping.class.getName()).log(Level.SEVERE, null, ex);
        }






    }

    private void OpenFileMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_OpenFileMenuItemActionPerformed
        // TODO add your handling code here:

        //  CountAllTerms_ = 0;
        CountAllTerms = 0;
        int CountNoun = 0;
        int CountAdjNoun = 0;
        int CountNounNoun = 0;
        int CountNounNounNoun = 0;
        int CountAdjAdjNoun = 0;
        int CountAbbr = 0;



        ArrayExportTerms = new ArrayList<String>();
        ArraySentences = new ArrayList<String>();
        ArrayTermsForLibWrite = new ArrayList<String>();
        ArrayTermsABC = new ArrayList<String>();
        ArrayTermsWcount = new ArrayList<String>();
        ArrayTermsNoun = new ArrayList<String>();
        listmodelNoun = new DefaultListModel();
        ArrayTermsAdjNoun = new ArrayList<String>();
        listmodelAdjNoun = new DefaultListModel();
        ArrayTermsNounNoun = new ArrayList<String>();
        listmodelNounNoun = new DefaultListModel();
        ArrayTermsNounNounNoun = new ArrayList<String>();
        listmodelNounNounNoun = new DefaultListModel();
        listmodelAdjAdjNoun = new DefaultListModel();
        ArrayTermsAdjAdjNoun = new ArrayList<String>();
        listmodelAbbr = new DefaultListModel();
        ArrayTermsAbbr = new ArrayList<String>();
        ArrayListForSentecesOutput = new ArrayList<String>();
        ArrayListForSentOutInOneWindow = new ArrayList<String>();
        //SBWcount = new SortByWcount[107];
        SBWcount = new SortByWcount[3000];
        SBDescending = new SortByDescending[3000];



        //TermsList.setTransferHandler(arrayListHandler);
        //TermsFavList.setTransferHandler(arrayListHandler);


        TermsList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        TermsFavList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);


        TermsFavList.setModel(model_);

        File KospektDll = new File("KonspektLib.dll");


        // if (!KospektDll.exists()) {
        if (ClientServerMode.isSelected()) {


            /////////////////////////////////////  
            //Работа в режиме кдиент-сервер при отсутствии библиотеки konspekt.dll в корневой папке программы 
            //или включённом в меню режиме клиент-сервер         
            ///////////////////////////////////////    


            System.out.println("Work with cloud server");


            String SelectedFileNameK = null;
            String SelectedFileNameKNew = null;

            int resultK = FileChooserOpen.showOpenDialog(null);
            if (resultK == JFileChooser.APPROVE_OPTION) {
                try {
                    //присваиваем метке1 имя открытого файла
                    SelectedFileCanonicalPath = FileChooserOpen.getSelectedFile().getCanonicalPath();
                    SelectedFileNameK = FileChooserOpen.getSelectedFile().getName();
                } catch (IOException ex) {
                    Logger.getLogger(SemanticMapping.class.getName()).log(Level.SEVERE, null, ex);
                }
                System.out.println("Absolute path to selected file -->> " + SelectedFileCanonicalPath);
                Object[] options = {"Да", "Нет!"};
                int n = JOptionPane.showOptionDialog(this.getOwner(), "Выбранный файл: " + SelectedFileCanonicalPath + " Проанализировать?",
                        "Подтверждение", JOptionPane.YES_NO_OPTION,
                        JOptionPane.QUESTION_MESSAGE, null, options,
                        options[0]);
                if (n == 0) {

                    System.out.println("Cloud operation");


                    FileInputStream fin;
                    // First make sure that a file has been specified
                    // on the command line.

                    // Now, open the file.
                    File f = new File(SelectedFileCanonicalPath);
                    try {
                        fin = new FileInputStream(f);
                    } catch (FileNotFoundException exc) {
                        System.out.println("File Not Found");
                        return;
                    }

                    byte fileContent[] = new byte[(int) f.length()];
                    try {
                        fin.read(fileContent);
                    } catch (IOException ex) {
                        Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
                    }


                    System.out.println("Send file to server for analyzing");

                    try {
                        this.sendBytes(fileContent);
                    } catch (IOException ex) {
                        Logger.getLogger(SemanticMapping.class.getName()).log(Level.SEVERE, null, ex);
                    }



                    System.out.println("Recieving file");

                    SelectedFileNameK = SelectedFileNameK.substring(0, SelectedFileNameK.indexOf("."));
                    SelectedFileNameKNew = SelectedFileNameK + ".tt";
                    FileOutputStream fout;
                    try {
                        // Open output file.
                        fout = new FileOutputStream(SelectedFileNameKNew);
                    } catch (FileNotFoundException exc) {
                        System.out.println("Error Opening Output File");
                        return;
                    }
                    try {
                        fout.write(this.readBytes());
                    } catch (IOException ex) {
                        Logger.getLogger(SemanticMapping.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    try {
                        fout.close();
                    } catch (IOException ex) {
                        Logger.getLogger(SemanticMapping.class.getName()).log(Level.SEVERE, null, ex);
                    }


                    System.out.println("Recieving file Done");


                    System.out.println("Show results to user in window");




                    //////////////////////////////////////  
                    //Обработка .tt файла и вывод результатов в окно программы   
                    ///////////////////////////////////////





                    //String TestTT = "/Users/MalahovKS/NetBeansProjects/InCom/Text/termsexport.tt";

                    BufferedReader br = null;

                    String S_sentences;

                    try {
                        try {
                            br = new BufferedReader(new InputStreamReader(new FileInputStream(SelectedFileNameKNew), ENCODING_WIN1251));
                        } catch (UnsupportedEncodingException ex) {
                            Logger.getLogger(SemanticMapping.class.getName()).log(Level.SEVERE, null, ex);
                            System.out.println("Unsupported Encoding!");
                        }

                    } catch (FileNotFoundException ex) {
                        Logger.getLogger(SemanticMapping.class.getName()).log(Level.SEVERE, null, ex);
                        System.out.println("File Not Found!");
                    }
                    try {

                        while ((S_sentences = br.readLine()) != null) {

                            ArraySentences.add(S_sentences);
                            ArrayExportTerms.add(S_sentences);
                        }
                    } catch (IOException ex) {
                        Logger.getLogger(SemanticMapping.class.getName()).log(Level.SEVERE, null, ex);
                    }


                    while (TagSentence.equals(ArraySentences.get(0)) != true) {
                        ArraySentences.remove(0);
                    }
                    ArraySentences.remove(0);


                    DefaultComboBoxModel comboboxModel1 = new DefaultComboBoxModel();
                    listmodel = new DefaultListModel();
//                dll_invoke.TextAnalysis(SelectedFile, CurrentDir()+"ParseFile.tt");
                    //Парсинг XLM-документа
//                File f_in = new File(SelectedFileCanonicalPath);


                    doc = new Document();


                    // List namedChildren = rootElement.getChildren("term");
                    SAXBuilder sb = new SAXBuilder();
                    try {
                        // помещение в память считываемого файла xml
                        doc = sb.build(new File(SelectedFileNameKNew));
                    } catch (JDOMException ex) {
                    } catch (IOException ex) {
                    }
                    Element rootElement = doc.getRootElement();
                    Element exportTerms = rootElement.getChild("exporterms");
                    Element sentences = rootElement.getChild("sentences");
                    List terms = exportTerms.getChildren("term");
                    java.util.Iterator i = terms.iterator();






                    while (i.hasNext()) {


                        Element term = (Element) i.next();
                        System.out.println(term.getChild("tname").getText()
                                + " wcount=" + term.getChild("wcount").getText()
                                + " sentposSize=" + term.getChildren("sentpos").size());
                        listmodel.addElement(term.getChild("tname").getText() + "(" + term.getChildren("sentpos").size() + ")");

                        ArrayTermsABC.add(term.getChild("tname").getText() + "(" + term.getChildren("sentpos").size() + ")");
                        ArrayTermsForLibWrite.add(term.getChild("tname").getText());


                        ArrayTermsWcount.add(term.getChildren("sentpos").size() + " " + term.getChild("tname").getText().toLowerCase());



                        SBWcount[CountAllTerms] = new SortByWcount(term.getChildren("sentpos").size(), term.getChild("tname").getText().toLowerCase());
                        SBDescending[CountAllTerms] = new SortByDescending(term.getChildren("sentpos").size(), term.getChild("tname").getText().toLowerCase());


                        CountAllTerms++;


                        if (_Noun.equals(term.getChild("ttype").getText())) {
                            ArrayTermsNoun.add(term.getChild("tname").getText() + "(" + term.getChildren("sentpos").size() + ")");
                            listmodelNoun.addElement(term.getChild("tname").getText() + "(" + term.getChildren("sentpos").size() + ")");
                            CountNoun++;

                        }

                        if (_AdjNoun.equals(term.getChild("ttype").getText())) {
                            ArrayTermsAdjNoun.add(term.getChild("tname").getText() + "(" + term.getChildren("sentpos").size() + ")");
                            listmodelAdjNoun.addElement(term.getChild("tname").getText() + "(" + term.getChildren("sentpos").size() + ")");
                            CountAdjNoun++;
                        }

                        if (_NounNoun.equals(term.getChild("ttype").getText())) {
                            ArrayTermsNounNoun.add(term.getChild("tname").getText() + "(" + term.getChildren("sentpos").size() + ")");
                            listmodelNounNoun.addElement(term.getChild("tname").getText() + "(" + term.getChildren("sentpos").size() + ")");
                            CountNounNoun++;
                        }

                        if (_NounNounNoun.equals(term.getChild("ttype").getText())) {
                            ArrayTermsNounNounNoun.add(term.getChild("tname").getText() + "(" + term.getChildren("sentpos").size() + ")");
                            listmodelNounNounNoun.addElement(term.getChild("tname").getText() + "(" + term.getChildren("sentpos").size() + ")");
                            CountNounNounNoun++;
                        }

                        if (_AdjAdjNoun.equals(term.getChild("ttype").getText())) {
                            ArrayTermsAdjAdjNoun.add(term.getChild("tname").getText() + "(" + term.getChildren("sentpos").size() + ")");
                            listmodelAdjAdjNoun.addElement(term.getChild("tname").getText() + "(" + term.getChildren("sentpos").size() + ")");
                            CountAdjAdjNoun++;
                        }

                        if (_Abbr.equals(term.getChild("ttype").getText())) {
                            ArrayTermsAbbr.add(term.getChild("tname").getText() + "(" + term.getChildren("sentpos").size() + ")");
                            listmodelAbbr.addElement(term.getChild("tname").getText() + "(" + term.getChildren("sentpos").size() + ")");
                            CountAbbr++;
                        }


                    }



                    for (int ii = 0; ii < ArraySentences.size(); ii++) {

                        System.out.println(ArraySentences.get(ii));

                    }



//Поиск слова в строке


                    comboboxModel1.addElement(allTerms_inComboBox + " (" + CountAllTerms + ")");
                    comboboxModel1.addElement(Nouns + " (" + CountNoun + ")");
                    comboboxModel1.addElement(AdjNoun + " (" + CountAdjNoun + ")");
                    comboboxModel1.addElement(NounNoun + " (" + CountNounNoun + ")");
                    comboboxModel1.addElement(NounNounNoun + " (" + CountNounNounNoun + ")");
                    comboboxModel1.addElement(AdjAdjNoun + " (" + CountAdjAdjNoun + ")");
                    comboboxModel1.addElement(Abbr + " (" + CountAbbr + ")");

                    jComboBox1.setModel(comboboxModel1);
                    TermsList.setModel(listmodel);
                    FindMenuItem.setEnabled(true);
                    SearchButton.setEnabled(true);


                    try {
                        br.close();
                    } catch (IOException ex) {
                        Logger.getLogger(SemanticMapping.class.getName()).log(Level.SEVERE, null, ex);
                    }






                    //////////////////////////////////////  
                    //Обработка .tt файла и вывод результатов в окно программы   
                    ///////////////////////////////////////       


                } else {

                    System.out.println("Analisys canceled");

                }
            } else {

                if (resultK == JFileChooser.ERROR_OPTION) {

                    System.out.println("Error while file selection");
                } else {
                    //если пользователь отменил диалог открытия файла, то
                    if (resultK == JFileChooser.CANCEL_OPTION) {

                        System.out.println(" <<< File don't selected >>> ");
                    }
                }
            }


        } else {

            if (!KonspektLibRunCheckBoxMenuItem.getState()) {

                //////////////////////////////////////  
                //
                //Работа библиотеки konspekt.dll локально с файлом разбора .tt
                /////////////////////////////////////// 


                BufferedReader br = null;


                //String SelectedFileCanonicalPath = null;
                String SelectedFileName = null;
                String S_sentences = null;

                int result = FileChooserOpen.showOpenDialog(null);
                if (result == JFileChooser.APPROVE_OPTION) {
                    try {
                        //присваиваем метке1 имя открытого файла
                        SelectedFileCanonicalPath = FileChooserOpen.getSelectedFile().getCanonicalPath();
                        SelectedFileName = FileChooserOpen.getSelectedFile().getName();
                    } catch (IOException ex) {
                        Logger.getLogger(SemanticMapping.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    System.out.println("Absolute path to selected file -->> " + SelectedFileCanonicalPath);


                    Object[] options = {"Да", "Нет!"};
                    int n = JOptionPane.showOptionDialog(this.getOwner(), "Выбранный файл: " + SelectedFileCanonicalPath + " Проанализировать?",
                            "Подтверждение", JOptionPane.YES_NO_OPTION,
                            JOptionPane.QUESTION_MESSAGE, null, options,
                            options[0]);
                    if (n == 0) {
                        try {
                            try {
                                br = new BufferedReader(new InputStreamReader(new FileInputStream(SelectedFileCanonicalPath), ENCODING_WIN1251));
                            } catch (UnsupportedEncodingException ex) {
                                Logger.getLogger(SemanticMapping.class.getName()).log(Level.SEVERE, null, ex);
                            }

                        } catch (FileNotFoundException ex) {
                            Logger.getLogger(SemanticMapping.class.getName()).log(Level.SEVERE, null, ex);
                        }
                        try {
                            while ((S_sentences = br.readLine()) != null) {

                                ArraySentences.add(S_sentences);
                                ArrayExportTerms.add(S_sentences);
                            }
                        } catch (IOException ex) {
                            Logger.getLogger(SemanticMapping.class.getName()).log(Level.SEVERE, null, ex);
                        }


// Get in ArraySentences only text sentences

                        while (TagSentence.equals(ArraySentences.get(0)) != true) {
                            ArraySentences.remove(0);
                        }
                        ArraySentences.remove(0);

                        // Get in ArraySentences only text sentences                       


                        // Get in ArrayExportTerms only <exporterms>                     

                        while (TagExporterms.equals(ArrayExportTerms.get(0)) != true) {
                            ArrayExportTerms.remove(0);
                        }
                        ArrayExportTerms.remove(0);

                        // Get in ArrayExportTerms only <exporterms>                  



                        DefaultComboBoxModel comboboxModel1 = new DefaultComboBoxModel();
                        listmodel = new DefaultListModel();
//                dll_invoke.TextAnalysis(SelectedFile, CurrentDir()+"ParseFile.tt");
                        //Парсинг XLM-документа
//                File f_in = new File(SelectedFileCanonicalPath);


                        doc = new Document();


                        // List namedChildren = rootElement.getChildren("term");
                        SAXBuilder sb = new SAXBuilder();
                        try {
                            // помещение в память считываемого файла xml
                            doc = sb.build(new File(SelectedFileCanonicalPath));
                        } catch (JDOMException ex) {
                        } catch (IOException ex) {
                        }
                        Element rootElement = doc.getRootElement();
                        Element exportTerms = rootElement.getChild("exporterms");
                        Element sentences = rootElement.getChild("sentences");
                        List terms = exportTerms.getChildren("term");
                        java.util.Iterator i = terms.iterator();






                        while (i.hasNext()) {


                            Element term = (Element) i.next();
                            System.out.println(term.getChild("tname").getText()
                                    + " wcount=" + term.getChild("wcount").getText()
                                    + " sentposSize=" + term.getChildren("sentpos").size());
                            listmodel.addElement(term.getChild("tname").getText() + "(" + term.getChildren("sentpos").size() + ")");

                            ArrayTermsABC.add(term.getChild("tname").getText() + "(" + term.getChildren("sentpos").size() + ")");
                            ArrayTermsForLibWrite.add(term.getChild("tname").getText());


                            ArrayTermsWcount.add(term.getChildren("sentpos").size() + " " + term.getChild("tname").getText().toLowerCase());



                            SBWcount[CountAllTerms] = new SortByWcount(term.getChildren("sentpos").size(), term.getChild("tname").getText().toLowerCase());
                            SBDescending[CountAllTerms] = new SortByDescending(term.getChildren("sentpos").size(), term.getChild("tname").getText().toLowerCase());


                            CountAllTerms++;


                            if (_Noun.equals(term.getChild("ttype").getText())) {
                                ArrayTermsNoun.add(term.getChild("tname").getText() + "(" + term.getChildren("sentpos").size() + ")");
                                listmodelNoun.addElement(term.getChild("tname").getText() + "(" + term.getChildren("sentpos").size() + ")");
                                CountNoun++;

                            }

                            if (_AdjNoun.equals(term.getChild("ttype").getText())) {
                                ArrayTermsAdjNoun.add(term.getChild("tname").getText() + "(" + term.getChildren("sentpos").size() + ")");
                                listmodelAdjNoun.addElement(term.getChild("tname").getText() + "(" + term.getChildren("sentpos").size() + ")");
                                CountAdjNoun++;
                            }

                            if (_NounNoun.equals(term.getChild("ttype").getText())) {
                                ArrayTermsNounNoun.add(term.getChild("tname").getText() + "(" + term.getChildren("sentpos").size() + ")");
                                listmodelNounNoun.addElement(term.getChild("tname").getText() + "(" + term.getChildren("sentpos").size() + ")");
                                CountNounNoun++;
                            }

                            if (_NounNounNoun.equals(term.getChild("ttype").getText())) {
                                ArrayTermsNounNounNoun.add(term.getChild("tname").getText() + "(" + term.getChildren("sentpos").size() + ")");
                                listmodelNounNounNoun.addElement(term.getChild("tname").getText() + "(" + term.getChildren("sentpos").size() + ")");
                                CountNounNounNoun++;
                            }

                            if (_AdjAdjNoun.equals(term.getChild("ttype").getText())) {
                                ArrayTermsAdjAdjNoun.add(term.getChild("tname").getText() + "(" + term.getChildren("sentpos").size() + ")");
                                listmodelAdjAdjNoun.addElement(term.getChild("tname").getText() + "(" + term.getChildren("sentpos").size() + ")");
                                CountAdjAdjNoun++;
                            }

                            if (_Abbr.equals(term.getChild("ttype").getText())) {
                                ArrayTermsAbbr.add(term.getChild("tname").getText() + "(" + term.getChildren("sentpos").size() + ")");
                                listmodelAbbr.addElement(term.getChild("tname").getText() + "(" + term.getChildren("sentpos").size() + ")");
                                CountAbbr++;
                            }


                        }



                        for (int ii = 0; ii < ArraySentences.size(); ii++) {

                            System.out.println(ArraySentences.get(ii));

                        }



//Поиск слова в строке


                        comboboxModel1.addElement(allTerms_inComboBox + " (" + CountAllTerms + ")");
                        comboboxModel1.addElement(Nouns + " (" + CountNoun + ")");
                        comboboxModel1.addElement(AdjNoun + " (" + CountAdjNoun + ")");
                        comboboxModel1.addElement(NounNoun + " (" + CountNounNoun + ")");
                        comboboxModel1.addElement(NounNounNoun + " (" + CountNounNounNoun + ")");
                        comboboxModel1.addElement(AdjAdjNoun + " (" + CountAdjAdjNoun + ")");
                        comboboxModel1.addElement(Abbr + " (" + CountAbbr + ")");

                        jComboBox1.setModel(comboboxModel1);
                        TermsList.setModel(listmodel);
                        FindMenuItem.setEnabled(true);
                        SearchButton.setEnabled(true);


                        try {
                            br.close();
                        } catch (IOException ex) {
                            Logger.getLogger(SemanticMapping.class.getName()).log(Level.SEVERE, null, ex);
                        }




                    } else {
                        System.out.println("Analisys canceled");

                    }



                } else {

                    if (result == JFileChooser.ERROR_OPTION) {

                        System.out.println("Error while file selection");
                    } else {
                        //если пользователь отменил диалог открытия файла, то
                        if (result == JFileChooser.CANCEL_OPTION) {

                            System.out.println(" <<< File don't selected >>> ");
                        }
                    }
                }

                //////////////////////////////////////  
                //
                //Работа библиотеки konspekt.dll локально с файлом разбора .tt
                ///////////////////////////////////////

            } else {


                //////////////////////////////////////  
                //Выбран пункт в главном меню - работа с реальным текстом;
                //Работа библиотеки konspekt.dll локально с реальным текстом
                ///////////////////////////////////////      



                System.out.println("With konspekt.dll run");


                BufferedReader br = null;


                //String SelectedFileCanonicalPath = null;
                String SelectedFileName = null;
                String S_sentences = null;

                int result = FileChooserOpen.showOpenDialog(null);
                if (result == JFileChooser.APPROVE_OPTION) {
                    try {
                        //присваиваем метке1 имя открытого файла
                        SelectedFileCanonicalPath = FileChooserOpen.getSelectedFile().getCanonicalPath();
                        SelectedFileName = FileChooserOpen.getSelectedFile().getName();
                    } catch (IOException ex) {
                        Logger.getLogger(SemanticMapping.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    System.out.println("Absolute path to selected file -->> " + SelectedFileCanonicalPath);


                    Object[] options = {"Да", "Нет!"};
                    int n = JOptionPane.showOptionDialog(this.getOwner(), "Выбранный файл: " + SelectedFileCanonicalPath + " Проанализировать?",
                            "Подтверждение", JOptionPane.YES_NO_OPTION,
                            JOptionPane.QUESTION_MESSAGE, null, options,
                            options[0]);
                    if (n == 0) {

                        // + работа dll библиотеки

//                    Runnable rAnimInThread = new AnimationInThread();
//                    tAnimInThread = new Thread(rAnimInThread);
//                    tAnimInThread.start();

                        System.out.println("+ работа dll библиотеки");

                        TextAnalysis(SelectedFileCanonicalPath, KonspektLibFileOut);

                        // tAnimInThread.stop();


                        try {
                            try {
                                br = new BufferedReader(new InputStreamReader(new FileInputStream(KonspektLibFileOut), ENCODING_WIN1251));
                            } catch (UnsupportedEncodingException ex) {
                                Logger.getLogger(SemanticMapping.class.getName()).log(Level.SEVERE, null, ex);
                                System.out.println("Unsupported Encoding!");
                            }

                        } catch (FileNotFoundException ex) {
                            Logger.getLogger(SemanticMapping.class.getName()).log(Level.SEVERE, null, ex);
                            System.out.println("File Not Found!");
                        }
                        try {
                            while ((S_sentences = br.readLine()) != null) {

                                ArraySentences.add(S_sentences);
                                ArrayExportTerms.add(S_sentences);
                            }
                        } catch (IOException ex) {
                            Logger.getLogger(SemanticMapping.class.getName()).log(Level.SEVERE, null, ex);
                        }


                        while (TagSentence.equals(ArraySentences.get(0)) != true) {
                            ArraySentences.remove(0);
                        }
                        ArraySentences.remove(0);


                        DefaultComboBoxModel comboboxModel1 = new DefaultComboBoxModel();
                        listmodel = new DefaultListModel();
//                dll_invoke.TextAnalysis(SelectedFile, CurrentDir()+"ParseFile.tt");
                        //Парсинг XLM-документа
//                File f_in = new File(SelectedFileCanonicalPath);


                        doc = new Document();


                        // List namedChildren = rootElement.getChildren("term");
                        SAXBuilder sb = new SAXBuilder();
                        try {
                            // помещение в память считываемого файла xml
                            doc = sb.build(new File(KonspektLibFileOut));
                        } catch (JDOMException ex) {
                        } catch (IOException ex) {
                        }
                        Element rootElement = doc.getRootElement();
                        Element exportTerms = rootElement.getChild("exporterms");
                        Element sentences = rootElement.getChild("sentences");
                        List terms = exportTerms.getChildren("term");
                        java.util.Iterator i = terms.iterator();






                        while (i.hasNext()) {


                            Element term = (Element) i.next();
                            System.out.println(term.getChild("tname").getText()
                                    + " wcount=" + term.getChild("wcount").getText()
                                    + " sentposSize=" + term.getChildren("sentpos").size());
                            listmodel.addElement(term.getChild("tname").getText() + "(" + term.getChildren("sentpos").size() + ")");

                            ArrayTermsABC.add(term.getChild("tname").getText() + "(" + term.getChildren("sentpos").size() + ")");
                            ArrayTermsForLibWrite.add(term.getChild("tname").getText());


                            ArrayTermsWcount.add(term.getChildren("sentpos").size() + " " + term.getChild("tname").getText().toLowerCase());



                            SBWcount[CountAllTerms] = new SortByWcount(term.getChildren("sentpos").size(), term.getChild("tname").getText().toLowerCase());
                            SBDescending[CountAllTerms] = new SortByDescending(term.getChildren("sentpos").size(), term.getChild("tname").getText().toLowerCase());


                            CountAllTerms++;


                            if (_Noun.equals(term.getChild("ttype").getText())) {
                                ArrayTermsNoun.add(term.getChild("tname").getText() + "(" + term.getChildren("sentpos").size() + ")");
                                listmodelNoun.addElement(term.getChild("tname").getText() + "(" + term.getChildren("sentpos").size() + ")");
                                CountNoun++;

                            }

                            if (_AdjNoun.equals(term.getChild("ttype").getText())) {
                                ArrayTermsAdjNoun.add(term.getChild("tname").getText() + "(" + term.getChildren("sentpos").size() + ")");
                                listmodelAdjNoun.addElement(term.getChild("tname").getText() + "(" + term.getChildren("sentpos").size() + ")");
                                CountAdjNoun++;
                            }

                            if (_NounNoun.equals(term.getChild("ttype").getText())) {
                                ArrayTermsNounNoun.add(term.getChild("tname").getText() + "(" + term.getChildren("sentpos").size() + ")");
                                listmodelNounNoun.addElement(term.getChild("tname").getText() + "(" + term.getChildren("sentpos").size() + ")");
                                CountNounNoun++;
                            }

                            if (_NounNounNoun.equals(term.getChild("ttype").getText())) {
                                ArrayTermsNounNounNoun.add(term.getChild("tname").getText() + "(" + term.getChildren("sentpos").size() + ")");
                                listmodelNounNounNoun.addElement(term.getChild("tname").getText() + "(" + term.getChildren("sentpos").size() + ")");
                                CountNounNounNoun++;
                            }

                            if (_AdjAdjNoun.equals(term.getChild("ttype").getText())) {
                                ArrayTermsAdjAdjNoun.add(term.getChild("tname").getText() + "(" + term.getChildren("sentpos").size() + ")");
                                listmodelAdjAdjNoun.addElement(term.getChild("tname").getText() + "(" + term.getChildren("sentpos").size() + ")");
                                CountAdjAdjNoun++;
                            }

                            if (_Abbr.equals(term.getChild("ttype").getText())) {
                                ArrayTermsAbbr.add(term.getChild("tname").getText() + "(" + term.getChildren("sentpos").size() + ")");
                                listmodelAbbr.addElement(term.getChild("tname").getText() + "(" + term.getChildren("sentpos").size() + ")");
                                CountAbbr++;
                            }


                        }



                        for (int ii = 0; ii < ArraySentences.size(); ii++) {

                            System.out.println(ArraySentences.get(ii));

                        }



//Поиск слова в строке


                        comboboxModel1.addElement(allTerms_inComboBox + " (" + CountAllTerms + ")");
                        comboboxModel1.addElement(Nouns + " (" + CountNoun + ")");
                        comboboxModel1.addElement(AdjNoun + " (" + CountAdjNoun + ")");
                        comboboxModel1.addElement(NounNoun + " (" + CountNounNoun + ")");
                        comboboxModel1.addElement(NounNounNoun + " (" + CountNounNounNoun + ")");
                        comboboxModel1.addElement(AdjAdjNoun + " (" + CountAdjAdjNoun + ")");
                        comboboxModel1.addElement(Abbr + " (" + CountAbbr + ")");

                        jComboBox1.setModel(comboboxModel1);
                        TermsList.setModel(listmodel);
                        FindMenuItem.setEnabled(true);
                        SearchButton.setEnabled(true);


                        try {
                            br.close();
                        } catch (IOException ex) {
                            Logger.getLogger(SemanticMapping.class.getName()).log(Level.SEVERE, null, ex);
                        }




                    } else {
                        System.out.println("Analisys canceled");

                    }



                } else {

                    if (result == JFileChooser.ERROR_OPTION) {

                        System.out.println("Error while file selection");
                    } else {
                        //если пользователь отменил диалог открытия файла, то
                        if (result == JFileChooser.CANCEL_OPTION) {

                            System.out.println(" <<< File don't selected >>> ");
                        }
                    }
                }

                //////////////////////////////////////  
                //Выбран пункт в главном меню - работа с реальным текстом;
                //Работа библиотеки konspekt.dll локально с реальным текстом
                /////////////////////////////////////// 


            }
        }
    }//GEN-LAST:event_OpenFileMenuItemActionPerformed

    private void DeleteFavButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_DeleteFavButtonActionPerformed
        // TODO add your handling code here:
        int[] arr = TermsFavList.getSelectedIndices();
        for (int i = arr.length - 1; i >= 0; --i) {
            model_.remove(arr[i]);
        }

    }//GEN-LAST:event_DeleteFavButtonActionPerformed

    private void alphabeticallyMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_alphabeticallyMenuItemActionPerformed
        // TODO add your handling code here:

        if (TermsList.getModel().equals(listmodel) | TermsList.getModel().equals(listmodel_1)) {
            TermsList.removeAll();
            listmodel_ = new DefaultListModel();
            System.out.println("Terms before sort:");
            System.out.println(ArrayTermsABC);

            Collections.sort(ArrayTermsABC);
            System.out.println("Terms after sort by ABC:");
            System.out.println(ArrayTermsABC);


            java.util.Iterator iter = ArrayTermsABC.iterator();
            while (iter.hasNext()) {
                listmodel_.addElement(iter.next().toString());

            }
            TermsList.setModel(listmodel_);

        }

        if (TermsList.getModel().equals(listmodel_2)) {
            TermsList.removeAll();
            listmodel_ = new DefaultListModel();
            Collections.sort(ArrayTermsNoun);
            System.out.println("Terms after sort by ABC:");
            System.out.println(ArrayTermsNoun);

            java.util.Iterator iter = ArrayTermsNoun.iterator();
            while (iter.hasNext()) {
                listmodel_.addElement(iter.next().toString());

            }
            TermsList.setModel(listmodel_);
        }


        if (TermsList.getModel().equals(listmodelNoun)) {
            TermsList.removeAll();
            listmodel_ = new DefaultListModel();
            Collections.sort(ArrayTermsNoun);
            System.out.println("Terms after sort by ABC:");
            System.out.println(ArrayTermsNoun);

            java.util.Iterator iter = ArrayTermsNoun.iterator();
            while (iter.hasNext()) {
                listmodel_.addElement(iter.next().toString());

            }
            TermsList.setModel(listmodel_);
        }


        if (TermsList.getModel().equals(listmodelNounNoun)) {
            TermsList.removeAll();
            listmodel_ = new DefaultListModel();
            Collections.sort(ArrayTermsNounNoun);
            System.out.println("Terms after sort by ABC:");
            System.out.println(ArrayTermsNounNoun);

            java.util.Iterator iter = ArrayTermsNounNoun.iterator();
            while (iter.hasNext()) {
                listmodel_.addElement(iter.next().toString());

            }
            TermsList.setModel(listmodel_);
        }


        if (TermsList.getModel().equals(listmodelNounNounNoun)) {
            TermsList.removeAll();

            listmodel_ = new DefaultListModel();
            Collections.sort(ArrayTermsNounNounNoun);
            System.out.println("Terms after sort by ABC:");
            System.out.println(ArrayTermsNounNounNoun);

            java.util.Iterator iter = ArrayTermsNounNounNoun.iterator();
            while (iter.hasNext()) {
                listmodel_.addElement(iter.next().toString());

            }
            TermsList.setModel(listmodel_);
        }

        if (TermsList.getModel().equals(listmodelAdjNoun)) {
            TermsList.removeAll();
            listmodel_ = new DefaultListModel();
            Collections.sort(ArrayTermsAdjNoun);
            System.out.println("Terms after sort by ABC:");
            System.out.println(ArrayTermsAdjNoun);

            java.util.Iterator iter = ArrayTermsAdjNoun.iterator();
            while (iter.hasNext()) {
                listmodel_.addElement(iter.next().toString());

            }
            TermsList.setModel(listmodel_);
        }


        if (TermsList.getModel().equals(listmodelAdjAdjNoun)) {
            TermsList.removeAll();
            listmodel_ = new DefaultListModel();
            Collections.sort(ArrayTermsAdjAdjNoun);
            System.out.println("Terms after sort by ABC:");
            System.out.println(ArrayTermsAdjAdjNoun);

            java.util.Iterator iter = ArrayTermsAdjAdjNoun.iterator();
            while (iter.hasNext()) {
                listmodel_.addElement(iter.next().toString());

            }
            TermsList.setModel(listmodel_);
        }



    }//GEN-LAST:event_alphabeticallyMenuItemActionPerformed

    private void WcountMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_WcountMenuItemActionPerformed


        SortByWcount[] SBWcountCopy = new SortByWcount[CountAllTerms];
        System.arraycopy(SBWcount, 0, SBWcountCopy, 0, CountAllTerms);

        java.util.Arrays.sort(SBWcountCopy);


        listmodel_1 = new DefaultListModel();


        System.out.println(CountAllTerms);

        System.out.println("-----------------------------------");





        if (TermsList.getModel().equals(listmodel_2)) {
            TermsList.removeAll();



            for (int i = 0; i < CountAllTerms; i++) {


                System.out.println(SBWcountCopy[i].Wcount + " " + SBWcountCopy[i].name);
                listmodel_1.addElement(SBWcountCopy[i].name + "(" + SBWcountCopy[i].Wcount + ")");



            }
            TermsList.setModel(listmodel_1);

        }


        if (TermsList.getModel().equals(listmodel_) | TermsList.getModel().equals(listmodelNoun)) {
            TermsList.removeAll();



            for (int i = 0; i < CountAllTerms; i++) {


                System.out.println(SBWcountCopy[i].Wcount + " " + SBWcountCopy[i].name);
                listmodel_1.addElement(SBWcountCopy[i].name + "(" + SBWcountCopy[i].Wcount + ")");



            }
            TermsList.setModel(listmodel_1);

        }

        if (TermsList.getModel().equals(listmodel) | TermsList.getModel().equals(listmodelNounNoun)) {
            TermsList.removeAll();



            for (int i = 0; i < CountAllTerms; i++) {


                System.out.println(SBWcountCopy[i].Wcount + " " + SBWcountCopy[i].name);
                listmodel_1.addElement(SBWcountCopy[i].name + "(" + SBWcountCopy[i].Wcount + ")");



            }
            TermsList.setModel(listmodel_1);

        }

        if (TermsList.getModel().equals(listmodelAdjAdjNoun)) {
            TermsList.removeAll();



            for (int i = 0; i < CountAllTerms; i++) {


                System.out.println(SBWcountCopy[i].Wcount + " " + SBWcountCopy[i].name);
                listmodel_1.addElement(SBWcountCopy[i].name + "(" + SBWcountCopy[i].Wcount + ")");



            }
            TermsList.setModel(listmodel_1);

        }


        if (TermsList.getModel().equals(listmodelNounNounNoun) | TermsList.getModel().equals(listmodelAdjNoun)) {
            TermsList.removeAll();

            for (int i = 0; i < CountAllTerms; i++) {

                System.out.println(SBWcountCopy[i].Wcount + " " + SBWcountCopy[i].name);
                listmodel_1.addElement(SBWcountCopy[i].name + "(" + SBWcountCopy[i].Wcount + ")");
            }
            TermsList.setModel(listmodel_1);

        }

    }//GEN-LAST:event_WcountMenuItemActionPerformed

    private void FindMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_FindMenuItemActionPerformed
        Runnable runner = new Runnable() {

            public void run() {
                frame = new JFrame("Пошук термінів");
                frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
                frame.setLocationRelativeTo(null);
                frame.setAlwaysOnTop(true);


                list = new FilteringJList();
                JScrollPane pane = new JScrollPane(list);
                frame.add(pane, BorderLayout.CENTER);
                JTextField text = new JTextField();
                list.installJTextField(text);
                frame.add(text, BorderLayout.NORTH);



                java.util.Iterator FindIterator = ArrayTermsABC.iterator();
                while (FindIterator.hasNext()) {

                    list.addElement(FindIterator.next().toString());
                }

                list.addListSelectionListener(new ListSelectionListener() {

                    public void valueChanged(ListSelectionEvent le) {

                        if (le.getValueIsAdjusting() == false) {
                            if (list.getSelectedIndex() == -1) {
                            } else {

                                int u = 0;
                                ArrayListForSentecesOutput.clear();
                                TerminThatSelectedInJlist = "";
                                ShowSentencesTextPane.setText("");
                                Element rootElement = doc.getRootElement();
                                Element exportTerms = rootElement.getChild("exporterms");
                                List terms = exportTerms.getChildren("term");



                                for (int i = 0; i < terms.size(); i++) {
                                    Element term = (Element) terms.get(i);
                                    TerminThatSelectedInJlist = list.getSelectedValue().toString();
                                    TerminThatSelectedInJlist = TerminThatSelectedInJlist.substring(0, TerminThatSelectedInJlist.indexOf("("));
                                    if (TerminThatSelectedInJlist.equalsIgnoreCase(term.getChild("tname").getText())) {


                                        if (TerminThatSelectedInJlist.contains(" ")) {


                                            NumberOfWordsInTermin = 0;
                                            Scanner ScannerSpaces = new Scanner(TerminThatSelectedInJlist.toString());
                                            while (ScannerSpaces.hasNext()) {
                                                System.out.println(ScannerSpaces.next().toString());
                                                NumberOfWordsInTermin++;

                                            }
                                            System.out.println(NumberOfWordsInTermin);

                                            List sentposes = term.getChildren("sentpos");

                                            NomerPredlogenia = new int[1000];
                                            NomerSlovaVPredlogenii = new int[1000];


                                            for (int j = 0; j < sentposes.size(); j++) {
                                                ttt++;
                                                int i2 = 0;
                                                int i3 = 0;
                                                Element sentpos = (Element) sentposes.get(j);
                                                String SentposValue = sentpos.getText();
                                                StringPos = SentposValue.substring(0, SentposValue.indexOf("/"));
                                                WordPosInSring = SentposValue.substring(SentposValue.indexOf("/"), SentposValue.length());
                                                WordPosInSring = WordPosInSring.substring(1, WordPosInSring.length());
                                                i2 = Integer.valueOf(StringPos).intValue();
                                                i3 = Integer.valueOf(WordPosInSring).intValue();
                                                NomerSlovaVPredlogenii[j] = i3;
                                                NomerPredlogenia[j] = i2;
                                                System.out.println("Позиция слова в предложении -->> " + i3);
                                                System.out.println("Word position in the sentence -->> " + i3);
                                                System.out.println("Номер предложения -->> " + i2);
                                                System.out.println("Number of sentence -->> " + i2);
                                                System.out.println("-->> " + (String) ArraySentences.get(i2));
                                                ArrayListForSentecesOutput.add((String) ArraySentences.get(i2));


                                                ScannerTextHighlightForTerminWithMultipleWords STHFTWMW = new ScannerTextHighlightForTerminWithMultipleWords();
                                                STHFTWMW.Scanner((String) ArraySentences.get(i2), i3, i2, ArraySentences, 1, SelectedFileCanonicalPath);


// Отображение предложений в отдельных окнах
                                                if (ShowSentencesInSeparateWinMenuItem.getState()) {

                                                    bw = new backlight_word_in_JtextPane((String) ArraySentences.get(i2) + " ", i3);
                                                    bw.setTitle("Термін - " + term.getChild("tname").getText());
                                                    posOfBw = posOfBw + 20;
                                                    bw.setLocation(posOfBw, posOfBw);

                                                }
// Отображение предложений в отдельных окнах


                                            }


                                        } else {


                                            List sentposes = term.getChildren("sentpos");

                                            NomerPredlogenia = new int[1000];
                                            NomerSlovaVPredlogenii = new int[1000];


                                            for (int j = 0; j < sentposes.size(); j++) {
                                                ttt++;
                                                int i2 = 0;
                                                int i3 = 0;
                                                Element sentpos = (Element) sentposes.get(j);
                                                String SentposValue = sentpos.getText();
                                                StringPos = SentposValue.substring(0, SentposValue.indexOf("/"));
                                                WordPosInSring = SentposValue.substring(SentposValue.indexOf("/"), SentposValue.length());
                                                WordPosInSring = WordPosInSring.substring(1, WordPosInSring.length());
                                                i2 = Integer.valueOf(StringPos).intValue();
                                                i3 = Integer.valueOf(WordPosInSring).intValue();
                                                NomerSlovaVPredlogenii[j] = i3;
                                                NomerPredlogenia[j] = i2;
                                                System.out.println("Позиция слова в предложении -->> " + i3);
                                                System.out.println("Word position in the sentence -->> " + i3);
                                                System.out.println("Номер предложения -->> " + i2);
                                                System.out.println("Number of sentence -->> " + i2);
                                                System.out.println("-->> " + (String) ArraySentences.get(i2));
                                                ArrayListForSentecesOutput.add((String) ArraySentences.get(i2));



// Отображение предложений в отдельных окнах Scanner

                                                ScannerTextHighlight scth = new ScannerTextHighlight();
                                                scth.Scanner((String) ArraySentences.get(i2), i3, i2, ArraySentences, SelectedFileCanonicalPath);

// Отображение предложений в отдельных окнах Scanner



                                                // Отображение предложений в отдельных окнах

                                                if (ShowSentencesInSeparateWinMenuItem.getState()) {

                                                    bw = new backlight_word_in_JtextPane((String) ArraySentences.get(i2) + " ", i3);
                                                    bw.setTitle("Термін - " + term.getChild("tname").getText());
                                                    posOfBw = posOfBw + 20;
                                                    bw.setLocation(posOfBw, posOfBw);

                                                }
// Отображение предложений в отдельных окнах


                                            }
                                        }
                                    }
                                }
                                System.out.println(ArrayListForSentecesOutput);
                                Log_OUT = ArrayListForSentecesOutput.toString();
                                Log_OUT = Log_OUT.substring(1, Log_OUT.length() - 1);

                                if (ShowSentWinSeparateCheckBoxMenuItem.getState()) {


                                    int NomerSlovaVPredlogeniiCopy[] = new int[ttt];
                                    System.arraycopy(NomerSlovaVPredlogenii, 0, NomerSlovaVPredlogeniiCopy, 0, ttt);

                                    int NomerPredlogeniaCopy[] = new int[ttt];
                                    System.arraycopy(NomerPredlogenia, 0, NomerPredlogeniaCopy, 0, ttt);



                                    bw1 = new BacklightWordInAllSentences(ArrayListForSentecesOutput, NomerSlovaVPredlogeniiCopy, NomerPredlogeniaCopy, ttt, u);
                                    bw1.setLocation(300, 100);


                                }
                                ttt = 0;
                                ShowSentencesTextPane.setText(Log_OUT);

                            }
                        }
                    }
                });




                frame.setSize(250, 150);
                frame.setVisible(true);
            }
        };
        EventQueue.invokeLater(runner);
    }//GEN-LAST:event_FindMenuItemActionPerformed

    private void jComboBox1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jComboBox1ActionPerformed
        // TODO add your handling code here:
        String item = (String) jComboBox1.getSelectedItem();
        if (item.startsWith(allTerms_inComboBox)) {
            TermsList.setModel(listmodel);

        }

        if (item.startsWith(Nouns)) {
            TermsList.setModel(listmodelNoun);


        }
        if (item.startsWith(AdjNoun)) {
            TermsList.setModel(listmodelAdjNoun);

        }
        if (item.startsWith(NounNoun)) {
            TermsList.setModel(listmodelNounNoun);

        }
        if (item.startsWith(NounNounNoun)) {
            TermsList.setModel(listmodelNounNounNoun);

        }
        if (item.startsWith(AdjAdjNoun)) {
            TermsList.setModel(listmodelAdjAdjNoun);

        }
        if (item.startsWith(Abbr)) {
            TermsList.setModel(listmodelAbbr);

        }

    }//GEN-LAST:event_jComboBox1ActionPerformed

    private void TermsListValueChanged(javax.swing.event.ListSelectionEvent evt) {//GEN-FIRST:event_TermsListValueChanged
//        if ((MultipleSelectionCheckBoxMenuItem.getState() != true) & (MultipleSelectionToggleButton.isSelected() != true)) {
//            MultipleSelectShowSentencesButton.setEnabled(false);
//            if (evt.getValueIsAdjusting() == false) {
//
//                if (TermsList.getSelectedIndex() == -1) {
//                } else {
//                    int u = 0;
//
//
//
//                    ArrayListForSentecesOutput.clear();
//                    TerminThatSelectedInJlist = "";
//                    ShowSentencesTextPane.setText("");
//                    Element rootElement = doc.getRootElement();
//                    Element exportTerms = rootElement.getChild("exporterms");
//                    List terms = exportTerms.getChildren("term");
//
//
//
//
//
//                    for (int i = 0; i < terms.size(); i++) {
//                        Element term = (Element) terms.get(i);
//                        TerminThatSelectedInJlist = TermsList.getSelectedValue().toString();
//                        TerminThatSelectedInJlist = TerminThatSelectedInJlist.substring(0, TerminThatSelectedInJlist.indexOf("("));
//                        if (TerminThatSelectedInJlist.equalsIgnoreCase(term.getChild("tname").getText())) {
//
//
//                            if (TerminThatSelectedInJlist.contains(" ")) {
//
//
//                                NumberOfWordsInTermin = 0;
//                                Scanner ScannerSpaces = new Scanner(TerminThatSelectedInJlist.toString());
//                                while (ScannerSpaces.hasNext()) {
//                                    System.out.println(ScannerSpaces.next().toString());
//                                    NumberOfWordsInTermin++;
//
//                                }
//                                System.out.println(NumberOfWordsInTermin);
//
//
//                                List sentposes = term.getChildren("sentpos");
//
//                                NomerPredlogenia = new int[1000];
//                                NomerSlovaVPredlogenii = new int[1000];
//
//
//                                for (int j = 0; j < sentposes.size(); j++) {
//                                    ttt++;
//                                    int i2 = 0;
//                                    int i3 = 0;
//                                    Element sentpos = (Element) sentposes.get(j);
//                                    String SentposValue = sentpos.getText();
//                                    StringPos = SentposValue.substring(0, SentposValue.indexOf("/"));
//                                    WordPosInSring = SentposValue.substring(SentposValue.indexOf("/"), SentposValue.length());
//                                    WordPosInSring = WordPosInSring.substring(1, WordPosInSring.length());
//                                    i2 = Integer.valueOf(StringPos).intValue();
//                                    i3 = Integer.valueOf(WordPosInSring).intValue();
//                                    NomerSlovaVPredlogenii[j] = i3;
//                                    NomerPredlogenia[j] = i2;
//                                    System.out.println("Позиция слова в предложении -->> " + i3);
//                                    System.out.println("Word position in the sentence -->> " + i3);
//                                    System.out.println("Номер предложения -->> " + i2);
//                                    System.out.println("Number of sentence -->> " + i2);
//                                    System.out.println("-->> " + (String) ArraySentences.get(i2));
//                                    ArrayListForSentecesOutput.add((String) ArraySentences.get(i2));
//
//
//                                    ScannerTextHighlightForTerminWithMultipleWords STHFTWMW = new ScannerTextHighlightForTerminWithMultipleWords();
//                                    STHFTWMW.Scanner((String) ArraySentences.get(i2), i3, i2, ArraySentences, 1, SelectedFileCanonicalPath);
//
//
//// Отображение предложений в отдельных окнах
//                                    if (ShowSentencesInSeparateWinMenuItem.getState()) {
//
//                                        bw = new backlight_word_in_JtextPane((String) ArraySentences.get(i2) + " ", i3);
//                                        bw.setTitle("Термін - " + term.getChild("tname").getText());
//                                        posOfBw = posOfBw + 20;
//                                        bw.setLocation(posOfBw, posOfBw);
//
//                                    }
//// Отображение предложений в отдельных окнах
//
//
//                                }
//
//
//                            } else {
//
//
//                                List sentposes = term.getChildren("sentpos");
//
//                                NomerPredlogenia = new int[1000];
//                                NomerSlovaVPredlogenii = new int[1000];
//
//
//                                for (int j = 0; j < sentposes.size(); j++) {
//                                    ttt++;
//                                    int i2 = 0;
//                                    int i3 = 0;
//                                    Element sentpos = (Element) sentposes.get(j);
//                                    String SentposValue = sentpos.getText();
//                                    StringPos = SentposValue.substring(0, SentposValue.indexOf("/"));
//                                    WordPosInSring = SentposValue.substring(SentposValue.indexOf("/"), SentposValue.length());
//                                    WordPosInSring = WordPosInSring.substring(1, WordPosInSring.length());
//                                    i2 = Integer.valueOf(StringPos).intValue();
//                                    i3 = Integer.valueOf(WordPosInSring).intValue();
//                                    NomerSlovaVPredlogenii[j] = i3;
//                                    NomerPredlogenia[j] = i2;
//                                    System.out.println("Позиция слова в предложении -->> " + i3);
//                                    System.out.println("Word position in the sentence -->> " + i3);
//                                    System.out.println("Номер предложения -->> " + i2);
//                                    System.out.println("Number of sentence -->> " + i2);
//                                    System.out.println("-->> " + (String) ArraySentences.get(i2));
//                                    ArrayListForSentecesOutput.add((String) ArraySentences.get(i2));
//
//
//
//// Отображение предложений в отдельных окнах Scanner
//
//                                    ScannerTextHighlight scth = new ScannerTextHighlight();
//                                    scth.Scanner((String) ArraySentences.get(i2), i3, i2, ArraySentences, SelectedFileCanonicalPath);
//
//// Отображение предложений в отдельных окнах Scanner
//
//
//
//                                    // Отображение предложений в отдельных окнах
//
//                                    if (ShowSentencesInSeparateWinMenuItem.getState()) {
//
//                                        bw = new backlight_word_in_JtextPane((String) ArraySentences.get(i2) + " ", i3);
//                                        bw.setTitle("Термін - " + term.getChild("tname").getText());
//                                        posOfBw = posOfBw + 20;
//                                        bw.setLocation(posOfBw, posOfBw);
//
//                                    }
//// Отображение предложений в отдельных окнах
//
//
//                                }
//                            }
//                        }
//                    }
//                    System.out.println(ArrayListForSentecesOutput);
//                    Log_OUT = ArrayListForSentecesOutput.toString();
//                    Log_OUT = Log_OUT.substring(1, Log_OUT.length() - 1);
//
//                    if (ShowSentWinSeparateCheckBoxMenuItem.getState()) {
//
//
//                        int NomerSlovaVPredlogeniiCopy[] = new int[ttt];
//                        System.arraycopy(NomerSlovaVPredlogenii, 0, NomerSlovaVPredlogeniiCopy, 0, ttt);
//
//                        int NomerPredlogeniaCopy[] = new int[ttt];
//                        System.arraycopy(NomerPredlogenia, 0, NomerPredlogeniaCopy, 0, ttt);
//
//
//
//                        bw1 = new BacklightWordInAllSentences(ArrayListForSentecesOutput, NomerSlovaVPredlogeniiCopy, NomerPredlogeniaCopy, ttt, u);
//                        bw1.setLocation(300, 100);
//
//
//                    }
//                    ttt = 0;
//                    ShowSentencesTextPane.setText(Log_OUT);
//
//                }
//            }
//
//            posOfBw = 10;
//
//        } else {
//
//            MultipleSelectShowSentencesButton.setEnabled(true);
//
//        }
    }//GEN-LAST:event_TermsListValueChanged

    private void jMenuItem2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem2ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jMenuItem2ActionPerformed

    private void jMenuItem3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem3ActionPerformed
        HighlightWord he = new HighlightWord();
        he.run("Народ здійснює", "Народ здійснює владу безпосередньо і через органи державної влади та органи місцевого самоврядування.");

        // HighlightWord he111 = new HighlightWord();
        // he111.run("Народ", "Народ здійснює владу безпосередньо і через органи державної влади та органи місцевого самоврядування.");
    }//GEN-LAST:event_jMenuItem3ActionPerformed

    private void jMenuItem4ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem4ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jMenuItem4ActionPerformed

    private void ShowPrefWinMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ShowPrefWinMenuItemActionPerformed
        // TODO add your handling code here:

        PreferecesWindow = new Prefereces();
        PreferecesWindow.setVisible(true);
}//GEN-LAST:event_ShowPrefWinMenuItemActionPerformed

    private void AdditionWinCheckBoxMenuItemStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_AdditionWinCheckBoxMenuItemStateChanged
        // TODO add your handling code here:

        if (AdditionWinCheckBoxMenuItem.getState()) {
            ForShowSentencesPanel.setVisible(true);
        }
        if (AdditionWinCheckBoxMenuItem.getState() == false) {
            ForShowSentencesPanel.setVisible(false);
        }
    }//GEN-LAST:event_AdditionWinCheckBoxMenuItemStateChanged

    private void MultipleSelectShowSentencesButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_MultipleSelectShowSentencesButtonActionPerformed


        //Необходимо u представить в виде массива int
        //
        //
        int u = 0;
        // TODO add your handling code here:
        ArrayNomerPredlogenia = new ArrayList<Integer>();
        ArrayNomerSlovaVPredlogenii = new ArrayList<Integer>();
        Object[] O = TermsList.getSelectedValues();
        ArrayListForSentecesOutput.clear();
        ArrayNomerPredlogenia.clear();
        ArrayNomerSlovaVPredlogenii.clear();
        TerminThatSelectedInJlist = "";
        ShowSentencesTextPane.setText("");
        ttt = 0;



        for (int i_ = 0; i_ < O.length; i_++) {

            Element rootElement = doc.getRootElement();
            Element exportTerms = rootElement.getChild("exporterms");
            List terms = exportTerms.getChildren("term");

            for (int i = 0; i < terms.size(); i++) {
                Element term = (Element) terms.get(i);
                TerminThatSelectedInJlist = O[i_].toString();
                TerminThatSelectedInJlist = TerminThatSelectedInJlist.substring(0, TerminThatSelectedInJlist.indexOf("("));

                if (TerminThatSelectedInJlist.equalsIgnoreCase(term.getChild("tname").getText())) {


                    if (TerminThatSelectedInJlist.contains(" ")) {

                        TerminThatSelectedInJlist = TerminThatSelectedInJlist.substring(TerminThatSelectedInJlist.indexOf(" "), TerminThatSelectedInJlist.length());
                        u = TerminThatSelectedInJlist.length();
                        List sentposes = term.getChildren("sentpos");

                        NomerPredlogenia = new int[1000];
                        NomerSlovaVPredlogenii = new int[1000];
                        //

                        for (int j = 0; j < sentposes.size(); j++) {
                            ttt++;
                            int i2 = 0;
                            int i3 = 0;
                            Element sentpos = (Element) sentposes.get(j);
                            String SentposValue = sentpos.getText();
                            StringPos = SentposValue.substring(0, SentposValue.indexOf("/"));
                            WordPosInSring = SentposValue.substring(SentposValue.indexOf("/"), SentposValue.length());
                            WordPosInSring = WordPosInSring.substring(1, WordPosInSring.length());
                            i2 = Integer.valueOf(StringPos).intValue();
                            i3 = Integer.valueOf(WordPosInSring).intValue();
                            NomerSlovaVPredlogenii[j] = i3;
                            NomerPredlogenia[j] = i2;
                            System.out.println("Позиция слова в предложении -->> " + i3);
                            System.out.println("Word position in sentence -->> " + i3);
                            System.out.println("Номер предложения -->> " + i2);
                            System.out.println("Number of sentence -->> " + i2);
                            System.out.println("-->> " + (String) ArraySentences.get(i2));
                            ArrayListForSentecesOutput.add((String) ArraySentences.get(i2));

                            if (ShowSentencesInSeparateWinMenuItem.getState()) {

                                bw = new backlight_word_in_JtextPane((String) ArraySentences.get(i2) + " ", i3);
                                bw.setTitle("Термін - " + term.getChild("tname").getText());
                                posOfBw = posOfBw + 20;
                                bw.setLocation(posOfBw, posOfBw);

                            }
                        }


                    } else {



                        List sentposes = term.getChildren("sentpos");

                        NomerPredlogenia = new int[1000];
                        NomerSlovaVPredlogenii = new int[1000];
                        //  ttt = 0;
                        for (int j = 0; j < sentposes.size(); j++) {
                            ttt++;
                            int i2 = 0;
                            int i3 = 0;
                            Element sentpos = (Element) sentposes.get(j);
                            String SentposValue = sentpos.getText();
                            StringPos = SentposValue.substring(0, SentposValue.indexOf("/"));
                            WordPosInSring = SentposValue.substring(SentposValue.indexOf("/"), SentposValue.length());
                            WordPosInSring = WordPosInSring.substring(1, WordPosInSring.length());
                            i2 = Integer.valueOf(StringPos).intValue();
                            i3 = Integer.valueOf(WordPosInSring).intValue();
                            NomerSlovaVPredlogenii[j] = i3;
                            NomerPredlogenia[j] = i2;
                            ArrayNomerSlovaVPredlogenii.add(i3);
                            ArrayNomerPredlogenia.add(i2);
                            System.out.println("Позиция слова в предложении -->> " + i3);
                            System.out.println("Word position in sentence -->> " + i3);
                            System.out.println("Номер предложения -->> " + i2);
                            System.out.println("Number of sentence -->> " + i2);
                            System.out.println("-->> " + (String) ArraySentences.get(i2));
                            ArrayListForSentecesOutput.add((String) ArraySentences.get(i2));


                        }
                    }

                }

            }



        }

        System.out.println(ArrayListForSentecesOutput);
        ShowSentencesTextPane.setText(ArrayListForSentecesOutput.toString());
        System.out.println(ArrayNomerPredlogenia);



        if (ShowSentWinSeparateCheckBoxMenuItem.getState()) {
            for (int r = 0; r < ArrayNomerPredlogenia.size(); r++) {

                NomerPredlogenia[r] = (Integer) ArrayNomerPredlogenia.get(r);
            }

            for (int r = 0; r < ArrayNomerPredlogenia.size(); r++) {
                DlyaNomeraSlovaVPredlogenii = ArrayNomerSlovaVPredlogenii.get(r).toString();
                NomerSlovaVPredlogenii[r] = Integer.valueOf(DlyaNomeraSlovaVPredlogenii).intValue();

            }

            System.out.println(NomerPredlogenia[0]);
            System.out.println(NomerPredlogenia[1]);



            int NomerSlovaVPredlogeniiCopy[] = new int[ttt];
            System.arraycopy(NomerSlovaVPredlogenii, 0, NomerSlovaVPredlogeniiCopy, 0, ttt);

            int NomerPredlogeniaCopy[] = new int[ttt];
            System.arraycopy(NomerPredlogenia, 0, NomerPredlogeniaCopy, 0, ttt);


            bw1 = new BacklightWordInAllSentences(ArrayListForSentecesOutput, NomerSlovaVPredlogeniiCopy, NomerPredlogeniaCopy, ttt, u);
            bw1.setLocation(300, 100);
            ttt = 0;
        }

    }//GEN-LAST:event_MultipleSelectShowSentencesButtonActionPerformed

    private void MultipleSelectionPropertyChange(java.beans.PropertyChangeEvent evt) {//GEN-FIRST:event_MultipleSelectionPropertyChange
        // TODO add your handling code here:
    }//GEN-LAST:event_MultipleSelectionPropertyChange

    private void TermsTreeCheckBoxMenuItemStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_TermsTreeCheckBoxMenuItemStateChanged
        // TODO add your handling code here:
        if (TermsTreeCheckBoxMenuItem.getState()) {
            TermsTreePanel.setVisible(true);
        }
        if (TermsTreeCheckBoxMenuItem.getState() == false) {
            TermsTreePanel.setVisible(false);
        }
    }//GEN-LAST:event_TermsTreeCheckBoxMenuItemStateChanged

    private void CloseModuleMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_CloseModuleMenuItemActionPerformed
        // TODO add your handling code here:
        this.dispose();
    }//GEN-LAST:event_CloseModuleMenuItemActionPerformed

    private void MultipleSelectionStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_MultipleSelectionStateChanged
        // TODO add your handling code here:

        if (MultipleSelectionCheckBoxMenuItem.getState()) {
            SelectFavButton.setEnabled(true);
            MultipleSelectShowSentencesButton.setEnabled(true);
            DeleteFavButton.setEnabled(true);
            ShowFavButton.setEnabled(true);
            SaveFavButton.setEnabled(true);
            TermsFavList.setEnabled(true);
            MultipleSelectionToggleButton.setSelected(true);

        }
        if (MultipleSelectionCheckBoxMenuItem.getState() == false) {
            SelectFavButton.setEnabled(false);
            MultipleSelectShowSentencesButton.setEnabled(false);
            DeleteFavButton.setEnabled(false);
            ShowFavButton.setEnabled(false);
            SaveFavButton.setEnabled(false);
            TermsFavList.setEnabled(false);
            MultipleSelectionToggleButton.setSelected(false);

        }
    }//GEN-LAST:event_MultipleSelectionStateChanged

    private void Wcount1MenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_Wcount1MenuItemActionPerformed

        SortByDescending[] SBWcountCopy1 = new SortByDescending[CountAllTerms];
        System.arraycopy(SBDescending, 0, SBWcountCopy1, 0, CountAllTerms);

        java.util.Arrays.sort(SBWcountCopy1);



        listmodel_2 = new DefaultListModel();


        System.out.println(CountAllTerms);

        System.out.println("-----------------------------------");



        if (TermsList.getModel().equals(listmodel_1)) {
            TermsList.removeAll();



            for (int i = 0; i < CountAllTerms; i++) {


                System.out.println(SBWcountCopy1[i].Wcount + " " + SBWcountCopy1[i].name);
                listmodel_2.addElement(SBWcountCopy1[i].name + "(" + SBWcountCopy1[i].Wcount + ")");



            }
            TermsList.setModel(listmodel_2);

        }


        if (TermsList.getModel().equals(listmodel_) | TermsList.getModel().equals(listmodelNoun)) {
            TermsList.removeAll();



            for (int i = 0; i < CountAllTerms; i++) {


                System.out.println(SBWcountCopy1[i].Wcount + " " + SBWcountCopy1[i].name);
                listmodel_2.addElement(SBWcountCopy1[i].name + "(" + SBWcountCopy1[i].Wcount + ")");



            }
            TermsList.setModel(listmodel_2);

        }

        if (TermsList.getModel().equals(listmodel) | TermsList.getModel().equals(listmodelNounNoun)) {
            TermsList.removeAll();



            for (int i = 0; i < CountAllTerms; i++) {


                System.out.println(SBWcountCopy1[i].Wcount + " " + SBWcountCopy1[i].name);
                listmodel_2.addElement(SBWcountCopy1[i].name + "(" + SBWcountCopy1[i].Wcount + ")");



            }
            TermsList.setModel(listmodel_2);

        }

        if (TermsList.getModel().equals(listmodelAdjAdjNoun)) {
            TermsList.removeAll();



            for (int i = 0; i < CountAllTerms; i++) {


                System.out.println(SBWcountCopy1[i].Wcount + " " + SBWcountCopy1[i].name);
                listmodel_2.addElement(SBWcountCopy1[i].name + "(" + SBWcountCopy1[i].Wcount + ")");



            }
            TermsList.setModel(listmodel_2);

        }


        if (TermsList.getModel().equals(listmodelNounNounNoun) | TermsList.getModel().equals(listmodelAdjNoun)) {
            TermsList.removeAll();



            for (int i = 0; i < CountAllTerms; i++) {


                System.out.println(SBWcountCopy1[i].Wcount + " " + SBWcountCopy1[i].name);
                listmodel_2.addElement(SBWcountCopy1[i].name + "(" + SBWcountCopy1[i].Wcount + ")");



            }
            TermsList.setModel(listmodel_2);

        }
    }//GEN-LAST:event_Wcount1MenuItemActionPerformed

    private void ShowFavButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ShowFavButtonActionPerformed
        // TODO add your handling code here:
        int u = 0;
        // TODO add your handling code here:
        ArrayNomerPredlogenia = new ArrayList<Integer>();
        ArrayNomerSlovaVPredlogenii = new ArrayList<Integer>();
        Object[] O = TermsFavList.getSelectedValues();
        ArrayListForSentecesOutput.clear();
        ArrayNomerPredlogenia.clear();
        ArrayNomerSlovaVPredlogenii.clear();
        TerminThatSelectedInJlist = "";
        ShowSentencesTextPane.setText("");
        ttt = 0;



        for (int i_ = 0; i_ < O.length; i_++) {

            Element rootElement = doc.getRootElement();
            Element exportTerms = rootElement.getChild("exporterms");
            List terms = exportTerms.getChildren("term");

            for (int i = 0; i < terms.size(); i++) {
                Element term = (Element) terms.get(i);
                TerminThatSelectedInJlist = O[i_].toString();
                TerminThatSelectedInJlist = TerminThatSelectedInJlist.substring(0, TerminThatSelectedInJlist.indexOf("("));

                if (TerminThatSelectedInJlist.equalsIgnoreCase(term.getChild("tname").getText())) {


                    List sentposes = term.getChildren("sentpos");

                    NomerPredlogenia = new int[1000];
                    NomerSlovaVPredlogenii = new int[1000];
                    //   ttt = 0;

                    for (int j = 0; j < sentposes.size(); j++) {
                        ttt++;
                        int i2 = 0;
                        int i3 = 0;
                        Element sentpos = (Element) sentposes.get(j);
                        String SentposValue = sentpos.getText();
                        StringPos = SentposValue.substring(0, SentposValue.indexOf("/"));
                        WordPosInSring = SentposValue.substring(SentposValue.indexOf("/"), SentposValue.length());
                        WordPosInSring = WordPosInSring.substring(1, WordPosInSring.length());
                        i2 = Integer.valueOf(StringPos).intValue();
                        i3 = Integer.valueOf(WordPosInSring).intValue();
                        NomerSlovaVPredlogenii[j] = i3;
                        NomerPredlogenia[j] = i2;
                        ArrayNomerSlovaVPredlogenii.add(i3);
                        ArrayNomerPredlogenia.add(i2);
                        System.out.println("Позиция слова в предложении -->> " + i3);
                        System.out.println("Word position in sentence -->> " + i3);
                        System.out.println("Номер предложения -->> " + i2);
                        System.out.println("Number of sentence -->> " + i2);
                        System.out.println("-->> " + (String) ArraySentences.get(i2));
                        ArrayListForSentecesOutput.add("<" + i2 + "> " + (String) ArraySentences.get(i2));


                    }

                }

            }

        }

        System.out.println(ArrayListForSentecesOutput);
        ShowSentencesTextPane.setText(ArrayListForSentecesOutput.toString());
        System.out.println(ArrayNomerPredlogenia);



        if (ShowSentWinSeparateCheckBoxMenuItem.getState()) {
            for (int r = 0; r < ArrayNomerPredlogenia.size(); r++) {
//            DlyaNomeraPredlogeniya = ArrayNomerPredlogenia.get(r).toString();
//            NomerPredlogenia[r] = Integer.valueOf(DlyaNomeraPredlogeniya).intValue();
                NomerPredlogenia[r] = (Integer) ArrayNomerPredlogenia.get(r);
            }

            for (int r = 0; r < ArrayNomerPredlogenia.size(); r++) {
                DlyaNomeraSlovaVPredlogenii = ArrayNomerSlovaVPredlogenii.get(r).toString();
                NomerSlovaVPredlogenii[r] = Integer.valueOf(DlyaNomeraSlovaVPredlogenii).intValue();

            }

            System.out.println(NomerPredlogenia[0]);
            System.out.println(NomerPredlogenia[1]);



            int NomerSlovaVPredlogeniiCopy[] = new int[ttt];
            System.arraycopy(NomerSlovaVPredlogenii, 0, NomerSlovaVPredlogeniiCopy, 0, ttt);

            int NomerPredlogeniaCopy[] = new int[ttt];
            System.arraycopy(NomerPredlogenia, 0, NomerPredlogeniaCopy, 0, ttt);


            bw1 = new BacklightWordInAllSentences(ArrayListForSentecesOutput, NomerSlovaVPredlogeniiCopy, NomerPredlogeniaCopy, ttt, u);
            bw1.setLocation(300, 100);
            ttt = 0;
        }


    }//GEN-LAST:event_ShowFavButtonActionPerformed

    private void SelectFavButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_SelectFavButtonActionPerformed



        Object[] o = TermsList.getSelectedValues();
        for (int i = 0; i < o.length; i++) {
            model_.addElement(o[i]);
        }


    }//GEN-LAST:event_SelectFavButtonActionPerformed

    private void SaveFavButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_SaveFavButtonActionPerformed

        if (model_.size() == 0) {

            System.out.println("Favorites list is empty");

        } else {
            FileChooserSave = new JFileChooser(System.getProperty("user.dir"));
            int result1 = FileChooserSave.showSaveDialog(null);
            if (result1 == JFileChooser.APPROVE_OPTION) {
                try {
                    f_result = FileChooserSave.getSelectedFile();
                    FileOutputStream fos = null;
                    try {
                        fos = new FileOutputStream(f_result);
                    } catch (FileNotFoundException ex) {
                        Logger.getLogger(SemanticMapping.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    BufferedWriter writer_ = null;
                    try {
                        writer_ = new BufferedWriter(new OutputStreamWriter(fos, "UTF-8"));
                    } catch (UnsupportedEncodingException ex) {
                        Logger.getLogger(SemanticMapping.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    for (int k = 0; k < model_.size(); k++) {
                        System.out.println(model_.get(k).toString());
                        try {
                            writer_.append(model_.get(k).toString());
                            writer_.append("\n");
                        } catch (IOException ex) {
                            Logger.getLogger(SemanticMapping.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }
                    writer_.close();
                    fos.close();
                } catch (IOException ex) {
                    Logger.getLogger(SemanticMapping.class.getName()).log(Level.SEVERE, null, ex);
                }

                Object[] options = {"OK"};
                try {
                    int n = JOptionPane.showOptionDialog(this.getOwner(), "Список збережено у файлі: " + f_result.getCanonicalPath() + "", "Підтвердження", JOptionPane.OK_OPTION, JOptionPane.INFORMATION_MESSAGE, null, options, options[0]);
                } catch (IOException ex) {
                    Logger.getLogger(SemanticMapping.class.getName()).log(Level.SEVERE, null, ex);
                }


            }

        }
    }//GEN-LAST:event_SaveFavButtonActionPerformed

    private void MultipleSelectionToggleButtonStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_MultipleSelectionToggleButtonStateChanged
        // TODO add your handling code here:

        if (MultipleSelectionToggleButton.isSelected()) {
            SelectFavButton.setEnabled(true);
            MultipleSelectShowSentencesButton.setEnabled(true);
            DeleteFavButton.setEnabled(true);
            ShowFavButton.setEnabled(true);
            SaveFavButton.setEnabled(true);
            TermsFavList.setEnabled(true);
            MultipleSelectionCheckBoxMenuItem.setSelected(true);
        }
        if (MultipleSelectionToggleButton.isSelected() == false) {
            SelectFavButton.setEnabled(false);
            MultipleSelectShowSentencesButton.setEnabled(false);
            DeleteFavButton.setEnabled(false);
            ShowFavButton.setEnabled(false);
            SaveFavButton.setEnabled(false);
            TermsFavList.setEnabled(false);
            MultipleSelectionCheckBoxMenuItem.setSelected(false);

        }

    }//GEN-LAST:event_MultipleSelectionToggleButtonStateChanged

    private void LibWriteButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_LibWriteButtonActionPerformed


        if (!ArrayTermsForLibWrite.isEmpty()) {
            this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
            try {
                // TODO add your handling code here:
                FileOutputStream fos1 = null;
                //CSVFileWrite = new File("write_doc.csv");
                try {
                    fos1 = new FileOutputStream(CSVFileWrite);
                } catch (FileNotFoundException ex) {
                    Logger.getLogger(SemanticMapping.class.getName()).log(Level.SEVERE, null, ex);
                }
                BufferedWriter writer1 = null;
                try {
                    writer1 = new BufferedWriter(new OutputStreamWriter(fos1, "windows-1251"));
                } catch (UnsupportedEncodingException ex) {
                    Logger.getLogger(SemanticMapping.class.getName()).log(Level.SEVERE, null, ex);
                }
                for (int k = 0; k < ArrayTermsForLibWrite.size(); k++) {
                    //System.out.println(model_.get(k).toString());
                    try {
                        writer1.append(SelectedFileCanonicalPath + ";" + ArrayTermsForLibWrite.get(k).toString() + ";");
                        writer1.append("\n");
                    } catch (IOException ex) {
                        Logger.getLogger(SemanticMapping.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
                writer1.close();
                fos1.close();

            } catch (IOException ex) {
                Logger.getLogger(SemanticMapping.class.getName()).log(Level.SEVERE, null, ex);
            }

        } else {
            System.out.println("Массив ArrayTermsForLibWrite пуст!!!");
            System.out.println("Array ArrayTermsForLibWrite is empty!!!");
            StatusBarTextField.setText("Массив ArrayTermsForLibWrite пуст!!!");
            this.setCursor(Cursor.getDefaultCursor());
        }

        if (CSVFileWrite.exists()) {
            this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
            Runtime r = Runtime.getRuntime();
            Process p = null;
            try {
                // выполняется Libwrite.exe

                String OSver = System.getProperty("os.name");
                System.out.println("OS Version -->" + OSver);

                if (OSver.startsWith("Win")) {
                    p = r.exec("Libwrite.exe ArmIndDoc.Dat write_doc.csv");
                } else {
                    if (OSver.startsWith("Mac")) {
                        String[] cmd = {"/bin/sh", "-c", "/opt/local/bin/wine /Users/kirilmalahov/NetBeansProjects/InCom/Libwrite.exe ArmIndDoc.DAT write_doc.csv"};
                        p = r.exec(cmd);

                    }
                }

                // jTextPane1.setText("Архів термінів створено");
                StatusBarTextField.setText("Архів термінів створено");
                try {
                    p.waitFor();

                    this.setCursor(Cursor.getDefaultCursor());
                } catch (InterruptedException ex) {
                    Logger.getLogger(testExeRun.class.getName()).log(Level.SEVERE, null, ex);
                }

            } catch (IOException ex) {
                Logger.getLogger(testExeRun.class.getName()).log(Level.SEVERE, null, ex);
            }
        } else {
            this.setCursor(Cursor.getDefaultCursor());
            System.out.println("write_doc.csv НЕ СОЗДАН");
            System.out.println("write_doc.csv didn't create");
            StatusBarTextField.setText("write_doc.csv НЕ СОЗДАН");
        }

    }//GEN-LAST:event_LibWriteButtonActionPerformed

    private void DeleteArmSpeedArchiveButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_DeleteArmSpeedArchiveButtonActionPerformed
        // TODO add your handling code here:
        //File DataBaseFile = new File("ArmIndDoc.DAT");

        this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

        if (DataBaseFile.exists()) {
            Boolean f_d = DataBaseFile.delete();
            System.out.println();
            // вывод в консоль инфо о удалении файла
            System.out.println("Удалён ли временный файл - " + f_d);
            System.out.println("temp file delete - " + f_d);
            //jTextPane1.setText("Архів термінів видалено");
            StatusBarTextField.setText("Архів термінів видалено");

        } else {
            // jTextPane1.setText("Архів термінів не створений");
            StatusBarTextField.setText("Архів термінів не створений");
            System.out.println();
            System.out.println("Временный файл отсутствует или небыл создан.");
            System.out.println("temp file didn't exist");
            System.out.println("Удалён ли временный файл - false");
            System.out.println("temp file delete - false");

        }

        if (CSVFileWrite.exists()) {
            Boolean f_d = CSVFileWrite.delete();
            System.out.println();
            // вывод в консоль инфо о удалении файла
            System.out.println("Удалён ли временный файл - " + f_d);
            System.out.println("temp file delete - " + f_d);
            //jTextPane1.setText("Архів термінів видалено");
            StatusBarTextField.setText("Архів термінів видалено");

        } else {
            // jTextPane1.setText("Архів термінів не створений");
            StatusBarTextField.setText("Архів термінів не створений");
            System.out.println();
            System.out.println("Временный файл отсутствует или небыл создан.");
            System.out.println("temp file didn't exist");
            System.out.println("Удалён ли временный файл - false");
            System.out.println("temp file delete - false");

        }
        try {
            Thread.sleep(1000);

        } catch (InterruptedException ex) {
            Logger.getLogger(SemanticMapping.class.getName()).log(Level.SEVERE, null, ex);
        }
        this.setCursor(Cursor.getDefaultCursor());

    }//GEN-LAST:event_DeleteArmSpeedArchiveButtonActionPerformed
    ImageTest ImFrame;

    class AnimationInThread implements Runnable {

        public void run() {
            ImFrame = new ImageTest();
            ImFrame.ShowFrame();

            //setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

        }
    }

    class LibReadInThread implements Runnable {

        public void run() {
            String S_libread;
            ArrayList ArrayLibread = new ArrayList<String>();

            setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
            Runtime r = Runtime.getRuntime();
            Process p = null;
            try {




                String OSver = System.getProperty("os.name");
                System.out.println("OS Version -->" + OSver);

                if (OSver.startsWith("Win")) {
                    p = r.exec("Libread.exe ArmIndDoc.DAT rrr_doc.csv " + ForLibReadTextField.getText());
                } else {
                    if (OSver.startsWith("Mac")) {
                        String[] cmd = {"/bin/sh", "-c", "/opt/local/bin/wine /Users/kirilmalahov/NetBeansProjects/InCom/Libread.exe ArmIndDoc.DAT rrr_doc.csv " + ForLibReadTextField.getText()};

                        p = r.exec(cmd);

                    }


                }
//p = r.exec("Libread.exe " + DataBaseFile.getAbsolutePath() + " rrr_doc.csv " + jTextField1.getText());
//String[] cmd = {"/bin/sh", "-c", "/opt/local/bin/wine /Users/kirilmalahov/NetBeansProjects/InCom/Libread.exe " + DataBaseFile.getAbsolutePath() + " /Users/kirilmalahov/NetBeansProjects/InCom/rrr_doc.csv " + jTextField1.getText()};

                System.out.println(DataBaseFile.getAbsolutePath());

                try {
                    p.waitFor();
                } catch (InterruptedException ex) {
                    Logger.getLogger(SemanticMapping.class.getName()).log(Level.SEVERE, null, ex);
                }
            } catch (IOException ex) {
                Logger.getLogger(SemanticMapping.class.getName()).log(Level.SEVERE, null, ex);
            }

            CSVFileRead = new File("rrr_doc.csv");

            try {
                br_libred = new BufferedReader(new InputStreamReader(new FileInputStream(CSVFileRead.getAbsolutePath()), "windows-1251"));
            } catch (IOException ex) {
                Logger.getLogger(SemanticMapping.class.getName()).log(Level.SEVERE, null, ex);
            }
            try {
                while ((S_libread = br_libred.readLine()) != null) {
                    ArrayLibread.add(S_libread);
                    ArrayLibread.add("\n");
                }
            } catch (IOException ex) {
                Logger.getLogger(SemanticMapping.class.getName()).log(Level.SEVERE, null, ex);
            }

            ForLibReadTextArea.setText(ArrayLibread.toString());
            ArrayLibread.clear();
            setCursor(Cursor.getDefaultCursor());

            ImFrame.HideFrame();


        }
    }
    Thread tAnimInThread;
    Thread tLibReadInThread;

    private void LibReadButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_LibReadButtonActionPerformed



//        if (!jTextField1.getText().isEmpty()) {
//
//
////            Runnable rAnimInThread = new AnimationInThread();
////            tAnimInThread = new Thread(rAnimInThread);
////            tAnimInThread.start();
//
//
//            Runnable rLibReadInThread = new LibReadInThread();
//            tLibReadInThread = new Thread(rLibReadInThread);
//            tLibReadInThread.start();
//
//        } else {
//            System.out.println("Строка поиска пуста!!! Введите термин!!!");
//            System.out.println("Search string is empty!!! Enter the term!!!");
//        }



        if (RedisCheckBoxMenuItem.isSelected()) {
            System.out.println("Redis");


        } else {

            String OSver = System.getProperty("os.name");
            System.out.println("OS Version -->" + OSver);

            if (OSver.startsWith("Win")) {

                if (!ForLibReadTextField.getText().isEmpty()) {
                    BufferedReader br_libred1 = null;

                    try {
                        String S_libread;
                        File DataFile = new File("ArmIndDoc.DAT");
                        File CSVFile = new File("rrr_doc.csv");
                        ArrayList ArrayLibread = new ArrayList<String>();
                        this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
                        Runtime r = Runtime.getRuntime();
                        Process p = null;
                        try {
                            p = r.exec("Libread.exe " + DataFile.getAbsolutePath() + " rrr_doc.csv " + ForLibReadTextField.getText());
                            try {
                                p.waitFor();
                            } catch (InterruptedException ex) {
                                Logger.getLogger(SemanticMapping.class.getName()).log(Level.SEVERE, null, ex);
                            }
                        } catch (IOException ex) {
                            Logger.getLogger(SemanticMapping.class.getName()).log(Level.SEVERE, null, ex);
                        }
                        try {
                            br_libred1 = new BufferedReader(new InputStreamReader(new FileInputStream(CSVFile.getAbsolutePath()), ENCODING_WIN1251));
                        } catch (FileNotFoundException ex) {
                            Logger.getLogger(ArchiveSearch.class.getName()).log(Level.SEVERE, null, ex);
                        }
                        try {
                            while ((S_libread = br_libred1.readLine()) != null) {
                                ArrayLibread.add(S_libread);
                            }
                        } catch (IOException ex) {
                            Logger.getLogger(ArchiveSearch.class.getName()).log(Level.SEVERE, null, ex);
                        }


                        System.out.println(ArrayLibread.toString());

                        String s = ArrayLibread.get(1).toString().substring(ArrayLibread.get(1).toString().indexOf(";") + 1, ArrayLibread.get(1).toString().length());

                        Document doc1 = new Document();


                        SAXBuilder sb = new SAXBuilder();
                        try {
                            // помещение в память считываемого файла xml
                            doc1 = sb.build(new File(s));
                        } catch (JDOMException ex) {
                        } catch (IOException ex) {
                        }
                        Element rootElement = doc1.getRootElement();
                        Element exportTerms = rootElement.getChild("exporterms");
                        Element sentences = rootElement.getChild("sentences");
                        List terms = exportTerms.getChildren("term");

                        int u = 0;
                        int ttt1 = 0;
                        int NumberOfWordsInTermin1;
                        String StringPos1;
                        String WordPosInSring1;
                        String S_sentences;

                        ArrayList ArrayListForSentecesOutput1 = new ArrayList<String>();
                        ArrayList ArraySentences1 = new ArrayList<String>();
                        BufferedReader br = null;

                        try {
                            try {
                                br = new BufferedReader(new InputStreamReader(new FileInputStream(s), ENCODING_WIN1251));
                            } catch (FileNotFoundException ex) {
                                Logger.getLogger(ArchiveSearch.class.getName()).log(Level.SEVERE, null, ex);
                            }

                        } catch (UnsupportedEncodingException ex) {
                            Logger.getLogger(SemanticMapping.class.getName()).log(Level.SEVERE, null, ex);
                        }

                        try {
                            while ((S_sentences = br.readLine()) != null) {

                                ArraySentences1.add(S_sentences);

                            }
                        } catch (IOException ex) {
                            Logger.getLogger(SemanticMapping.class.getName()).log(Level.SEVERE, null, ex);
                        }


                        while (TagSentence.equals(ArraySentences1.get(0)) != true) {
                            ArraySentences1.remove(0);
                        }
                        ArraySentences1.remove(0);



                        ArrayListForSentecesOutput1.clear();
                        String TerminThatSelectedInJlist1 = "";

                        for (int i = 0; i < terms.size(); i++) {
                            Element term = (Element) terms.get(i);
                            TerminThatSelectedInJlist1 = ArrayLibread.get(0).toString();

                            if (TerminThatSelectedInJlist1.equalsIgnoreCase(term.getChild("tname").getText())) {


                                if (TerminThatSelectedInJlist1.contains(" ")) {


                                    NumberOfWordsInTermin1 = 0;
                                    Scanner ScannerSpaces = new Scanner(TerminThatSelectedInJlist1.toString());
                                    while (ScannerSpaces.hasNext()) {
                                        System.out.println(ScannerSpaces.next().toString());
                                        NumberOfWordsInTermin1++;

                                    }
                                    System.out.println(NumberOfWordsInTermin1);


                                    List sentposes = term.getChildren("sentpos");

                                    int[] NomerPredlogenia1 = new int[1000];
                                    int[] NomerSlovaVPredlogenii1 = new int[1000];


                                    for (int j = 0; j < sentposes.size(); j++) {
                                        ttt1++;
                                        int i2 = 0;
                                        int i3 = 0;
                                        Element sentpos = (Element) sentposes.get(j);
                                        String SentposValue = sentpos.getText();
                                        StringPos1 = SentposValue.substring(0, SentposValue.indexOf("/"));
                                        WordPosInSring1 = SentposValue.substring(SentposValue.indexOf("/"), SentposValue.length());
                                        WordPosInSring1 = WordPosInSring1.substring(1, WordPosInSring1.length());
                                        i2 = Integer.valueOf(StringPos1).intValue();
                                        i3 = Integer.valueOf(WordPosInSring1).intValue();
                                        NomerSlovaVPredlogenii1[j] = i3;
                                        NomerPredlogenia1[j] = i2;
                                        System.out.println("Позиция слова в предложении -->> " + i3);
                                        System.out.println("Word position in the sentence -->> " + i3);
                                        System.out.println("Номер предложения -->> " + i2);
                                        System.out.println("Number of sentence -->> " + i2);
                                        System.out.println("-->> " + (String) ArraySentences1.get(i2));
                                        ArrayListForSentecesOutput1.add((String) ArraySentences1.get(i2));


                                    }


                                } else {


                                    List sentposes = term.getChildren("sentpos");

                                    int[] NomerPredlogenia1 = new int[1000];
                                    int[] NomerSlovaVPredlogenii1 = new int[1000];


                                    for (int j = 0; j < sentposes.size(); j++) {
                                        ttt1++;
                                        int i2 = 0;
                                        int i3 = 0;
                                        Element sentpos = (Element) sentposes.get(j);
                                        String SentposValue = sentpos.getText();
                                        StringPos1 = SentposValue.substring(0, SentposValue.indexOf("/"));
                                        WordPosInSring1 = SentposValue.substring(SentposValue.indexOf("/"), SentposValue.length());
                                        WordPosInSring1 = WordPosInSring1.substring(1, WordPosInSring1.length());
                                        i2 = Integer.valueOf(StringPos1).intValue();
                                        i3 = Integer.valueOf(WordPosInSring1).intValue();
                                        NomerSlovaVPredlogenii1[j] = i3;
                                        NomerPredlogenia1[j] = i2;
                                        System.out.println("Позиция слова в предложении -->> " + i3);
                                        System.out.println("Word position in the sentence -->> " + i3);
                                        System.out.println("Номер предложения -->> " + i2);
                                        System.out.println("Number of sentence -->> " + i2);
                                        System.out.println("-->> " + (String) ArraySentences1.get(i2));
                                        ArrayListForSentecesOutput1.add((String) ArraySentences1.get(i2));

                                    }
                                }
                            }
                        }
                        System.out.println(ArrayListForSentecesOutput1);



                        ForLibReadTextArea.setText(ArrayLibread.get(0) + "\n" + ArrayLibread.get(1) + "\n" + ArrayListForSentecesOutput1.toString());
                        ArrayLibread.clear();
                        this.setCursor(Cursor.getDefaultCursor());
                    } catch (UnsupportedEncodingException ex) {
                        Logger.getLogger(ArchiveSearch.class.getName()).log(Level.SEVERE, null, ex);
                    } finally {
                        try {
                            br_libred1.close();
                        } catch (IOException ex) {
                            Logger.getLogger(ArchiveSearch.class.getName()).log(Level.SEVERE, null, ex);
                        }

                    }
                } else {
                    System.out.println("Строка поиска пуста!!! Введите термин!!!");
                    System.out.println("Search string is empty!!! Enter the term!!!");
                }
            }

            if (OSver.startsWith("Mac")) {
                System.out.println("This option isn't avaliable for Mac OS X");
            }



        }

    }//GEN-LAST:event_LibReadButtonActionPerformed

    private void MenuLibWriteMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_MenuLibWriteMenuItemActionPerformed
        // TODO add your handling code here:


        if (RedisCheckBoxMenuItem.isSelected()) {
            System.out.println("Redis");


            //////////////////////////////////////  
            //Для работы клиента Redis необходимо задать  ip адрес сервера на котором установлена БД Redis
            //по умолчанию задан 127.0.0.1           
            ///////////////////////////////////////       

            Jedis jedis = new Jedis("162.243.143.167");

            //String DomainArea = SelectedFileCanonicalPath.substring(SelectedFileCanonicalPath.lastIndexOf("/") + 1, SelectedFileCanonicalPath.length());

            for (int k_red = 0; k_red < ArrayTermsForLibWrite.size(); k_red++) {

                jedis.set(ArrayTermsForLibWrite.get(k_red).toString(), SelectedFileCanonicalPath);
                //проверка наличия термина в базе Redis
//                if (jedis.get(ArrayTermsForLibWrite.get(k_red).toString()).equals("null")) {
//
//                    jedis.set(ArrayTermsForLibWrite.get(k_red).toString(), SelectedFileCanonicalPath);
//
//
//
//
//                    //jedis.hset(DomainArea, ArrayTermsForLibWrite.get(k).toString(), SelectedFileCanonicalPath);
//
//
//
//
//
//
//
//                } else {
//
//                    System.out.println("Term already in Redis");
//
//
//                }
            }

            System.out.println("Redis Done");

        } else {

            if (!ArrayTermsForLibWrite.isEmpty()) {
                this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
                try {
                    // TODO add your handling code here:
                    FileOutputStream fos1 = null;
                    //CSVFileWrite = new File("write_doc.csv");
                    try {
                        fos1 = new FileOutputStream(CSVFileWrite);
                    } catch (FileNotFoundException ex) {
                        Logger.getLogger(SemanticMapping.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    BufferedWriter writer1 = null;
                    try {
                        writer1 = new BufferedWriter(new OutputStreamWriter(fos1, "windows-1251"));
                    } catch (UnsupportedEncodingException ex) {
                        Logger.getLogger(SemanticMapping.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    for (int k = 0; k < ArrayTermsForLibWrite.size(); k++) {
                        //System.out.println(model_.get(k).toString());
                        try {
                            writer1.append(SelectedFileCanonicalPath + ";" + ArrayTermsForLibWrite.get(k).toString() + ";");
                            writer1.append("\n");
                        } catch (IOException ex) {
                            Logger.getLogger(SemanticMapping.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }
                    writer1.close();
                    fos1.close();

                } catch (IOException ex) {
                    Logger.getLogger(SemanticMapping.class.getName()).log(Level.SEVERE, null, ex);
                }

            } else {
                System.out.println("Массив ArrayTermsForLibWrite пуст!!!");
                System.out.println("Array ArrayTermsForLibWrite is empty!!!");
                StatusBarTextField.setText("Массив ArrayTermsForLibWrite пуст!!!");
                this.setCursor(Cursor.getDefaultCursor());
            }

            if (CSVFileWrite.exists()) {
                this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
                Runtime r = Runtime.getRuntime();
                Process p = null;
                try {
                    // выполняется Libwrite.exe

                    String OSver = System.getProperty("os.name");
                    System.out.println("OS Version -->" + OSver);

                    if (OSver.startsWith("Win")) {
                        p = r.exec("Libwrite.exe ArmIndDoc.Dat write_doc.csv");
                    } else {
                        if (OSver.startsWith("Mac")) {
                            String[] cmd = {"/bin/sh", "-c", "/opt/local/bin/wine /Users/kirilmalahov/NetBeansProjects/InCom/Libwrite.exe ArmIndDoc.DAT write_doc.csv"};
                            p = r.exec(cmd);

                        }
                    }

                    // jTextPane1.setText("Архів термінів створено");
                    StatusBarTextField.setText("Архів термінів створено");
                    try {
                        p.waitFor();

                        this.setCursor(Cursor.getDefaultCursor());
                    } catch (InterruptedException ex) {
                        Logger.getLogger(testExeRun.class.getName()).log(Level.SEVERE, null, ex);
                    }

                } catch (IOException ex) {
                    Logger.getLogger(testExeRun.class.getName()).log(Level.SEVERE, null, ex);
                }
            } else {
                this.setCursor(Cursor.getDefaultCursor());
                System.out.println("write_doc.csv НЕ СОЗДАН");
                System.out.println("write_doc.csv didn't create");
                StatusBarTextField.setText("write_doc.csv НЕ СОЗДАН");
            }
        }
    }//GEN-LAST:event_MenuLibWriteMenuItemActionPerformed

    private void DeleteArchiveMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_DeleteArchiveMenuItemActionPerformed
        // TODO add your handling code here:
        this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

        if (DataBaseFile.exists()) {
            Boolean f_d = DataBaseFile.delete();
            System.out.println();
            // вывод в консоль инфо о удалении файла
            System.out.println("Удалён ли временный файл - " + f_d);
            System.out.println("temp file delete - " + f_d);
            // jTextPane1.setText("Архів термінів видалено");
            StatusBarTextField.setText("Архів термінів видалено");

        } else {
            //jTextPane1.setText("Архів термінів не створений");
            StatusBarTextField.setText("Архів термінів не створений");
            System.out.println();
            System.out.println("Временный файл отсутствует или небыл создан.");
            System.out.println("temp file didn't exist");
            System.out.println("Удалён ли временный файл - false");
            System.out.println("temp file delete - false");

        }

        if (CSVFileWrite.exists()) {
            Boolean f_d = CSVFileWrite.delete();
            System.out.println();
            // вывод в консоль инфо о удалении файла
            System.out.println("Удалён ли временный файл - " + f_d);
            System.out.println("temp file delete - " + f_d);
            //jTextPane1.setText("Архів термінів видалено");
            StatusBarTextField.setText("Архів термінів видалено");

        } else {
            // jTextPane1.setText("Архів термінів не створений");
            StatusBarTextField.setText("Архів термінів не створений");
            System.out.println();
            System.out.println("Временный файл отсутствует или небыл создан.");
            System.out.println("temp file didn't exist");
            System.out.println("Удалён ли временный файл - false");
            System.out.println("temp file delete - false");

        }
        try {
            Thread.sleep(1000);

        } catch (InterruptedException ex) {
            Logger.getLogger(SemanticMapping.class.getName()).log(Level.SEVERE, null, ex);
        }
        this.setCursor(Cursor.getDefaultCursor());



    }//GEN-LAST:event_DeleteArchiveMenuItemActionPerformed

    private void DeleteForLibReadButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_DeleteForLibReadButtonActionPerformed
        this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

        if (DataBaseFile.exists()) {
            Boolean f_d = DataBaseFile.delete();
            System.out.println();
            // вывод в консоль инфо о удалении файла
            System.out.println("Удалён ли временный файл - " + f_d);
            System.out.println("temp file delete - " + f_d);
            //jTextPane1.setText("Архів термінів видалено");
            StatusBarTextField.setText("Архів термінів видалено");
        } else {
            // jTextPane1.setText("Архів термінів не створений");
            StatusBarTextField.setText("Архів термінів не створений");
            System.out.println();
            System.out.println("Временный файл отсутствует или небыл создан.");
            System.out.println("temp file didn't exist");
            System.out.println("Удалён ли временный файл - false");
            System.out.println("temp file delete - false");

        }

        if (CSVFileWrite.exists()) {
            Boolean f_d = CSVFileWrite.delete();
            System.out.println();
            // вывод в консоль инфо о удалении файла
            System.out.println("Удалён ли временный файл - " + f_d);
            System.out.println("temp file delete - " + f_d);
            //jTextPane1.setText("Архів термінів видалено");
            StatusBarTextField.setText("Архів термінів видалено");

        } else {
            //jTextPane1.setText("Архів термінів не створений");
            StatusBarTextField.setText("Архів термінів не створений");
            System.out.println();
            System.out.println("Временный файл отсутствует или небыл создан.");
            System.out.println("temp file didn't exist");
            System.out.println("Удалён ли временный файл - false");
            System.out.println("temp file delete - false");

        }
        try {
            Thread.sleep(1000);

        } catch (InterruptedException ex) {
            Logger.getLogger(SemanticMapping.class.getName()).log(Level.SEVERE, null, ex);
        }
        this.setCursor(Cursor.getDefaultCursor());
    }//GEN-LAST:event_DeleteForLibReadButtonActionPerformed

    private void MultipleSelectionToggleButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_MultipleSelectionToggleButtonActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_MultipleSelectionToggleButtonActionPerformed

    private void MultipleSelectionActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_MultipleSelectionActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_MultipleSelectionActionPerformed

    private void ShowSentencesInSeparateWinMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ShowSentencesInSeparateWinMenuItemActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_ShowSentencesInSeparateWinMenuItemActionPerformed

    private void AdditionToolsMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_AdditionToolsMenuItemActionPerformed
        // TODO add your handling code here:

        MainToolBar.setVisible(true);
        DeleteArchiveMenuItem.setVisible(true);
        DeleteForLibReadButton.setVisible(true);
        ShowPrefWinMenuItem.setVisible(true);
        ShowSentencesInSeparateWinMenuItem.setVisible(true);
        jMenuItem2.setVisible(true);
        jMenuItem3.setVisible(true);
        jMenuItem4.setVisible(true);
    }//GEN-LAST:event_AdditionToolsMenuItemActionPerformed

    // Функция Jtree to text 
    private static String getTreeText(TreeModel model, Object object, String indent) {
        String myRow = indent + object + "\n";
        for (int i = 0; i < model.getChildCount(object); i++) {
            myRow += getTreeText(model, model.getChild(object, i), indent + "  ");
        }
        return myRow;
    }

    // Функция Jtree to text
    private void TermsListMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_TermsListMouseClicked


        // Counts of mouse click == 2

        if (evt.getClickCount() == 2) {



            if ((MultipleSelectionCheckBoxMenuItem.getState() != true) & (MultipleSelectionToggleButton.isSelected() != true)) {
                MultipleSelectShowSentencesButton.setEnabled(false);




                if (TermsList.getSelectedIndex() == -1) {
                } else {
                    int u = 0;



                    ArrayListForSentecesOutput.clear();
                    TerminThatSelectedInJlist = "";
                    ShowSentencesTextPane.setText("");
                    Element rootElement = doc.getRootElement();
                    Element exportTerms = rootElement.getChild("exporterms");
                    List terms = exportTerms.getChildren("term");





                    for (int i = 0; i < terms.size(); i++) {
                        Element term = (Element) terms.get(i);
                        TerminThatSelectedInJlist = TermsList.getSelectedValue().toString();
                        TerminThatSelectedInJlist = TerminThatSelectedInJlist.substring(0, TerminThatSelectedInJlist.indexOf("("));










                        if (TerminThatSelectedInJlist.equalsIgnoreCase(term.getChild("tname").getText())) {


                            //для терминов с пробелами (многословные термины)
                            if (TerminThatSelectedInJlist.contains(" ")) {


                                NumberOfWordsInTermin = 0;
                                Scanner ScannerSpaces = new Scanner(TerminThatSelectedInJlist.toString());
                                while (ScannerSpaces.hasNext()) {
                                    System.out.println(ScannerSpaces.next().toString());
                                    NumberOfWordsInTermin++;

                                }
                                System.out.println(NumberOfWordsInTermin);


                                List sentposes = term.getChildren("sentpos");

                                NomerPredlogenia = new int[1000];
                                NomerSlovaVPredlogenii = new int[1000];


                                for (int j = 0; j < sentposes.size(); j++) {
                                    ttt++;
                                    int i2 = 0;
                                    int i3 = 0;
                                    Element sentpos = (Element) sentposes.get(j);
                                    String SentposValue = sentpos.getText();
                                    StringPos = SentposValue.substring(0, SentposValue.indexOf("/"));
                                    WordPosInSring = SentposValue.substring(SentposValue.indexOf("/"), SentposValue.length());
                                    WordPosInSring = WordPosInSring.substring(1, WordPosInSring.length());
                                    i2 = Integer.valueOf(StringPos).intValue();
                                    i3 = Integer.valueOf(WordPosInSring).intValue();
                                    NomerSlovaVPredlogenii[j] = i3;
                                    NomerPredlogenia[j] = i2;
                                    System.out.println("Позиция слова в предложении -->> " + i3);
                                    System.out.println("Word position in the sentence -->> " + i3);
                                    System.out.println("Номер предложения -->> " + i2);
                                    System.out.println("Number of sentence -->> " + i2);
                                    System.out.println("-->> " + (String) ArraySentences.get(i2));
                                    ArrayListForSentecesOutput.add((String) ArraySentences.get(i2));


                                    ScannerTextHighlightForTerminWithMultipleWords STHFTWMW = new ScannerTextHighlightForTerminWithMultipleWords();
                                    STHFTWMW.Scanner((String) ArraySentences.get(i2), i3, i2, ArraySentences, 1, SelectedFileCanonicalPath);


// Отображение предложений в отдельных окнах
                                    if (ShowSentencesInSeparateWinMenuItem.getState()) {

                                        bw = new backlight_word_in_JtextPane((String) ArraySentences.get(i2) + " ", i3);
                                        bw.setTitle("Термін - " + term.getChild("tname").getText());
                                        posOfBw = posOfBw + 20;
                                        bw.setLocation(posOfBw, posOfBw);

                                    }
// Отображение предложений в отдельных окнах


                                }



                                //Tree build reldown

                                DefaultMutableTreeNode RootDefaultMutableTreeNode = new DefaultMutableTreeNode(TerminThatSelectedInJlist);
                                TreeModel = new DefaultTreeModel(RootDefaultMutableTreeNode);

                                Element reldown = null;


                                try {
                                    reldown = term.getChild("reldown");


                                    String tnameString;
                                    tnameString = ArrayExportTerms.get(Integer.valueOf(reldown.getText())).toString();
                                    tnameString = tnameString.substring(tnameString.indexOf("<tname>"), tnameString.indexOf("</tname>"));
                                    System.out.println("Reldown tname = " + tnameString);
                                    tnameString = tnameString.substring(tnameString.indexOf(">"), tnameString.length());
                                    System.out.println("Reldown tname = " + tnameString);


                                    // RootDefaultMutableTreeNode.add(new DefaultMutableTreeNode(ArrayExportTerms.get(Integer.valueOf(relup.getText()))));

                                    RootDefaultMutableTreeNode.add(new DefaultMutableTreeNode(tnameString));


                                    //TermsTree.setModel(TreeModel);

                                } catch (NullPointerException e) {

                                    System.out.println("no reldown relation");
                                }


//Tree build reldown        

//Tree build relup

//                                DefaultMutableTreeNode RootDefaultMutableTreeNode = new DefaultMutableTreeNode(TerminThatSelectedInJlist);
//                                DefaultTreeModel TreeModel = new DefaultTreeModel(RootDefaultMutableTreeNode);

                                Element relup = null;


                                try {
                                    relup = term.getChild("relup");


                                    String tnameString;
                                    tnameString = ArrayExportTerms.get(Integer.valueOf(relup.getText())).toString();
                                    tnameString = tnameString.substring(tnameString.indexOf("<tname>"), tnameString.indexOf("</tname>"));
                                    System.out.println("Relup tname = " + tnameString);
                                    tnameString = tnameString.substring(tnameString.indexOf(">"), tnameString.length());
                                    System.out.println("Relup tname = " + tnameString);


                                    // RootDefaultMutableTreeNode.add(new DefaultMutableTreeNode(ArrayExportTerms.get(Integer.valueOf(relup.getText()))));

                                    RootDefaultMutableTreeNode.add(new DefaultMutableTreeNode(tnameString));


                                    //TermsTree.setModel(TreeModel);

                                } catch (NullPointerException e) {

                                    System.out.println("no relup relation");
                                }


                                //если в термине с пробелами нет Reldown -> crash
                                TermsTree.setModel(TreeModel);

//Tree build relup                                        

                                //Инициализация функции Jtree to text

                                System.out.println(getTreeText(TreeModel, TreeModel.getRoot(), ""));

                                //Инициализация функции Jtree to text

                            } else {

//Для терминов без пробелов (однословные термины)

//Tree build relup

                                DefaultMutableTreeNode root = new DefaultMutableTreeNode(TerminThatSelectedInJlist);
                                DefaultTreeModel model = new DefaultTreeModel(root);

                                Element relup = null;


                                try {
                                    relup = term.getChild("relup");


                                    String tnameString;
                                    tnameString = ArrayExportTerms.get(Integer.valueOf(relup.getText())).toString();
                                    tnameString = tnameString.substring(tnameString.indexOf("<tname>"), tnameString.indexOf("</tname>"));
                                    System.out.println("Relup tname = " + tnameString);
                                    tnameString = tnameString.substring(tnameString.indexOf(">"), tnameString.length());
                                    System.out.println("Relup tname = " + tnameString);


                                    // RootDefaultMutableTreeNode.add(new DefaultMutableTreeNode(ArrayExportTerms.get(Integer.valueOf(relup.getText()))));

                                    root.add(new DefaultMutableTreeNode(tnameString));


                                    TermsTree.setModel(model);

                                } catch (NullPointerException e) {
                                    TermsTree.setModel(model);
                                    System.out.println("no relup relation");
                                }


//Tree build relup                           




                                List sentposes = term.getChildren("sentpos");

                                NomerPredlogenia = new int[1000];
                                NomerSlovaVPredlogenii = new int[1000];


                                for (int j = 0; j < sentposes.size(); j++) {
                                    ttt++;
                                    int i2 = 0;
                                    int i3 = 0;
                                    Element sentpos = (Element) sentposes.get(j);
                                    String SentposValue = sentpos.getText();
                                    StringPos = SentposValue.substring(0, SentposValue.indexOf("/"));
                                    WordPosInSring = SentposValue.substring(SentposValue.indexOf("/"), SentposValue.length());
                                    WordPosInSring = WordPosInSring.substring(1, WordPosInSring.length());
                                    i2 = Integer.valueOf(StringPos).intValue();
                                    i3 = Integer.valueOf(WordPosInSring).intValue();
                                    NomerSlovaVPredlogenii[j] = i3;
                                    NomerPredlogenia[j] = i2;
                                    System.out.println("Позиция слова в предложении -->> " + i3);
                                    System.out.println("Word position in the sentence -->> " + i3);
                                    System.out.println("Номер предложения -->> " + i2);
                                    System.out.println("Number of sentence -->> " + i2);
                                    System.out.println("-->> " + (String) ArraySentences.get(i2));
                                    ArrayListForSentecesOutput.add((String) ArraySentences.get(i2));



// Отображение предложений в отдельных окнах Scanner

                                    ScannerTextHighlight scth = new ScannerTextHighlight();
                                    scth.Scanner((String) ArraySentences.get(i2), i3, i2, ArraySentences, SelectedFileCanonicalPath);

// Отображение предложений в отдельных окнах Scanner



                                    // Отображение предложений в отдельных окнах

                                    if (ShowSentencesInSeparateWinMenuItem.getState()) {

                                        bw = new backlight_word_in_JtextPane((String) ArraySentences.get(i2) + " ", i3);
                                        bw.setTitle("Термін - " + term.getChild("tname").getText());
                                        posOfBw = posOfBw + 20;
                                        bw.setLocation(posOfBw, posOfBw);

                                    }
// Отображение предложений в отдельных окнах


                                }
                            }
                        }
                    }
                    System.out.println(ArrayListForSentecesOutput);
                    Log_OUT = ArrayListForSentecesOutput.toString();
                    Log_OUT = Log_OUT.substring(1, Log_OUT.length() - 1);

                    if (ShowSentWinSeparateCheckBoxMenuItem.getState()) {


                        int NomerSlovaVPredlogeniiCopy[] = new int[ttt];
                        System.arraycopy(NomerSlovaVPredlogenii, 0, NomerSlovaVPredlogeniiCopy, 0, ttt);

                        int NomerPredlogeniaCopy[] = new int[ttt];
                        System.arraycopy(NomerPredlogenia, 0, NomerPredlogeniaCopy, 0, ttt);



                        bw1 = new BacklightWordInAllSentences(ArrayListForSentecesOutput, NomerSlovaVPredlogeniiCopy, NomerPredlogeniaCopy, ttt, u);
                        bw1.setLocation(300, 100);


                    }
                    ttt = 0;
                    ShowSentencesTextPane.setText(Log_OUT);

                }


                posOfBw = 10;

            } else {

                MultipleSelectShowSentencesButton.setEnabled(true);

            }



        }



    }//GEN-LAST:event_TermsListMouseClicked

    private void jMenuItem1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem1ActionPerformed
        // TODO add your handling code here:

        FileChooserSave = new JFileChooser(System.getProperty("user.dir"));
        int result1 = FileChooserSave.showSaveDialog(null);
        if (result1 == JFileChooser.APPROVE_OPTION) {
            try {
                f_result = FileChooserSave.getSelectedFile();
                FileOutputStream fos = null;
                try {
                    fos = new FileOutputStream(f_result);
                } catch (FileNotFoundException ex) {
                    Logger.getLogger(SemanticMapping.class.getName()).log(Level.SEVERE, null, ex);
                }
                BufferedWriter writer_ = null;
                try {
                    writer_ = new BufferedWriter(new OutputStreamWriter(fos, "UTF-8"));
                } catch (UnsupportedEncodingException ex) {
                    Logger.getLogger(SemanticMapping.class.getName()).log(Level.SEVERE, null, ex);
                }
                writer_.append(getTreeText(TreeModel, TreeModel.getRoot(), ""));


//                    for (int k = 0; k < model_.size(); k++) {
//                        System.out.println(model_.get(k).toString());
//                        try {
//                            writer_.append(model_.get(k).toString());
//                            writer_.append("\n");
//                        } catch (IOException ex) {
//                            Logger.getLogger(SemanticMapping.class.getName()).log(Level.SEVERE, null, ex);
//                        }
//                    }
                writer_.close();
                fos.close();
            } catch (IOException ex) {
                Logger.getLogger(SemanticMapping.class.getName()).log(Level.SEVERE, null, ex);
            }

            Object[] options = {"OK"};
            try {
                int n = JOptionPane.showOptionDialog(this.getOwner(), "Список збережено у файлі: " + f_result.getCanonicalPath() + "", "Підтвердження", JOptionPane.OK_OPTION, JOptionPane.INFORMATION_MESSAGE, null, options, options[0]);
            } catch (IOException ex) {
                Logger.getLogger(SemanticMapping.class.getName()).log(Level.SEVERE, null, ex);
            }


        }



    }//GEN-LAST:event_jMenuItem1ActionPerformed

    private void jMenuItem5ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem5ActionPerformed
        // TODO add your handling code here:

        SelectFavButton.setEnabled(true);
        DeleteFavButton.setEnabled(true);
        ShowFavButton.setEnabled(true);
        SaveFavButton.setEnabled(true);
        TermsFavList.setEnabled(true);
    }//GEN-LAST:event_jMenuItem5ActionPerformed

    private void MultipleSelectionCheckBoxMenuItemStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_MultipleSelectionCheckBoxMenuItemStateChanged
        // TODO add your handling code here:
    }//GEN-LAST:event_MultipleSelectionCheckBoxMenuItemStateChanged

    private void MultipleSelectionCheckBoxMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_MultipleSelectionCheckBoxMenuItemActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_MultipleSelectionCheckBoxMenuItemActionPerformed

    private void RedisCheckBoxMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_RedisCheckBoxMenuItemActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_RedisCheckBoxMenuItemActionPerformed

    private void jMenuItem6ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem6ActionPerformed
        // TODO add your handling code here:

        Port_IP.setVisible(true);
    }//GEN-LAST:event_jMenuItem6ActionPerformed

    private void TermsTreeCheckBoxMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_TermsTreeCheckBoxMenuItemActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_TermsTreeCheckBoxMenuItemActionPerformed

    private void buttonLigCorpusCollectionMongoBDActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonLigCorpusCollectionMongoBDActionPerformed
        // TODO add your handling code here:
        
       
        
    }//GEN-LAST:event_buttonLigCorpusCollectionMongoBDActionPerformed

    private void buttonOntologiesCollectionMongoBDActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonOntologiesCollectionMongoBDActionPerformed
        // TODO add your handling code here:
        
                credentialForMongoBD = MongoCredential.createMongoCRCredential("Opanasenko", "pld_ontology", passForMongoDB.toCharArray());
        try {
            mongoClient = new MongoClient(new ServerAddress("ds057000.mongolab.com", 57000), java.util.Arrays.asList(credentialForMongoBD));
        } catch (UnknownHostException ex) {
            Logger.getLogger(SemanticMapping.class.getName()).log(Level.SEVERE, null, ex);
        }
        mongodbPldOntology = mongoClient.getDB("pld_ontology");
        gfsLinguisticCorpus = new GridFS(mongodbPldOntology, "ontologies");
        
        
        
        
        
        // print the result from 'linguistic_corpus' collection
            DBCursor cursor = gfsLinguisticCorpus.getFileList();
            
            jTextArea1.setText("");
            while (cursor.hasNext()) {
                fileNameGFSLinguisticCorpus = cursor.next().toString();
                        
                System.out.println(fileNameGFSLinguisticCorpus);
                jTextArea1.append(fileNameGFSLinguisticCorpus);
                jTextArea1.append("\n");
                //obj = new JSONObject(cursor.next());
                //System.out.println(fileNameGFSLinguisticCorpus = obj.getJSONObject("_id").getString("filename"));
                //listModelMongoDB.addElement(cursor.next().toString());
            }

    }//GEN-LAST:event_buttonOntologiesCollectionMongoBDActionPerformed

    private void buttonAllCollectionMongoBDActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonAllCollectionMongoBDActionPerformed
        // TODO add your handling code here:
        
        credentialForMongoBD = MongoCredential.createMongoCRCredential("Opanasenko", "pld_ontology", passForMongoDB.toCharArray());
        try {
            mongoClient = new MongoClient(new ServerAddress("ds057000.mongolab.com", 57000), java.util.Arrays.asList(credentialForMongoBD));
        } catch (UnknownHostException ex) {
            Logger.getLogger(SemanticMapping.class.getName()).log(Level.SEVERE, null, ex);
        }
        mongodbPldOntology = mongoClient.getDB("pld_ontology");
        gfsLinguisticCorpus = new GridFS(mongodbPldOntology, "ontologies");
        
        // print the result from 'linguistic_corpus' collection
            DBCursor cursor = gfsLinguisticCorpus.getFileList();
            while (cursor.hasNext()) {
                System.out.println(cursor.next());
            }
            
          java.util.Set<String> colls = mongodbPldOntology.getCollectionNames();

            for (String s : colls) {
                System.out.println(s);
                listModelMongoDB.addElement(s);
            }  
           
            //listModelMongoDB.
        
        
    }//GEN-LAST:event_buttonAllCollectionMongoBDActionPerformed

    //Для контекстного меню tableConfor
    public void addRowTableConfor() {

        tableModel.addRow(new String[0]);

    }

    private void removeCurrentRowTableConfor() {
        int selectedRow = tableConfor.getSelectedRow();
        tableModel.removeRow(selectedRow);
    }

    private void removeCurrentColumnTableConfor() {
        int selectedColumn = tableConfor.getSelectedColumn();
        tableConfor.removeColumn(tableConfor.getColumnModel().getColumn(selectedColumn));
    }

    public void addColumnTableConfor() {
        tableModel.addColumn(new String[0]);
    }

    private void copySelectedCellTableConfor() {
        selectedCellForCopy = (String) tableConfor.getValueAt(tableConfor.getSelectedRow(), tableConfor.getSelectedColumn());
        System.out.println(selectedCellForCopy);

    }

    private void pasteSelectedCellTableConfor() {
        //String selectedCell = (String) table.getValueAt(table.getSelectedRow(), table.getSelectedColumn());
        tableConfor.setValueAt(selectedCellForCopy, tableConfor.getSelectedRow(), tableConfor.getSelectedColumn());
        //System.out.println(selectedCell);
    }

    private void removeAllRowsTableConfor() {
        int rowCount = tableModel.getRowCount();
        for (int i = 0; i < rowCount; i++) {
            tableModel.removeRow(0);
        }
    }

    public void toFileTableConfor(JTable table, File file) throws IOException {

        //TableModel model = table.getModel();
        FileWriter excel = new FileWriter(file);
        for (int i = 0; i < tableModel.getRowCount(); i++) {
            for (int j = 0; j < tableModel.getColumnCount(); j++) {
//                    excel.write(model.getValueAt(i,j).toString()+"\t");
                if (tableModel.getValueAt(i, j) == null) {
                    excel.write("" + ",");
                } else {
                    excel.write(tableModel.getValueAt(i, j) + ",");
                }
            }
            excel.write("\n");
        }
        excel.close();


    }

    //Для контекстного меню tableConfor
    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {


        java.awt.EventQueue.invokeLater(new Runnable() {

            @Override
            public void run() {
                try {
                    UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());//getSystemLookAndFeelClassName());
                } catch (ClassNotFoundException ex) {
                    Logger.getLogger(SemanticMapping.class.getName()).log(Level.SEVERE, null, ex);
                } catch (InstantiationException ex) {
                    Logger.getLogger(SemanticMapping.class.getName()).log(Level.SEVERE, null, ex);
                } catch (IllegalAccessException ex) {
                    Logger.getLogger(SemanticMapping.class.getName()).log(Level.SEVERE, null, ex);
                } catch (UnsupportedLookAndFeelException ex) {
                    Logger.getLogger(SemanticMapping.class.getName()).log(Level.SEVERE, null, ex);
                }
                new SemanticMapping().setVisible(true);


            }
        });
    }
    
    
    
    private static final String passForMongoDB = "pld";
    private MongoCredential credentialForMongoBD;
    private MongoClient mongoClient;
    private DB mongodbPldOntology;
    private GridFS gfsLinguisticCorpus;
    
    //private JSONObject obj;
    private String fileNameGFSLinguisticCorpus;
    
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JMenuItem AdditionToolsMenuItem;
    private javax.swing.JCheckBoxMenuItem AdditionWinCheckBoxMenuItem;
    private javax.swing.JPanel ArcSearchPanel;
    private javax.swing.JCheckBoxMenuItem ClientServerMode;
    private javax.swing.JMenuItem CloseModuleMenuItem;
    private javax.swing.JMenuItem DeleteArchiveMenuItem;
    private javax.swing.JButton DeleteArmSpeedArchiveButton;
    private javax.swing.JButton DeleteFavButton;
    private javax.swing.JButton DeleteForLibReadButton;
    private javax.swing.JPanel FavTermsPanel;
    private javax.swing.JFileChooser FileChooserOpen;
    private javax.swing.JMenu FileMenu;
    private javax.swing.JMenuItem FindMenuItem;
    private javax.swing.JTextArea ForLibReadTextArea;
    private javax.swing.JTextField ForLibReadTextField;
    private javax.swing.JPanel ForShowSentencesPanel;
    private javax.swing.JCheckBoxMenuItem KonspektLibRunCheckBoxMenuItem;
    private javax.swing.JButton LibReadButton;
    private javax.swing.JButton LibWriteButton;
    private javax.swing.JMenuBar MainMenuBar;
    private javax.swing.JPanel MainSemMapPanel;
    private javax.swing.JToolBar MainToolBar;
    private javax.swing.JMenuItem MenuLibWriteMenuItem;
    private javax.swing.JPanel MongoDBPanel;
    private javax.swing.JButton MultipleSelectShowSentencesButton;
    private javax.swing.JCheckBoxMenuItem MultipleSelectionCheckBoxMenuItem;
    private javax.swing.JToggleButton MultipleSelectionToggleButton;
    private javax.swing.JButton OpenFileButton;
    private javax.swing.JMenuItem OpenFileMenuItem;
    private javax.swing.JMenu PreferencesMenu;
    private javax.swing.JMenu PropertiesMenu;
    private javax.swing.JCheckBoxMenuItem RedisCheckBoxMenuItem;
    private javax.swing.JButton SaveFavButton;
    private javax.swing.JButton SearchButton;
    private javax.swing.JButton SelectFavButton;
    private javax.swing.JTabbedPane SemanticMappingTabbedPane;
    private javax.swing.JButton ShowFavButton;
    private javax.swing.JMenuItem ShowPrefWinMenuItem;
    private javax.swing.JCheckBoxMenuItem ShowSentWinSeparateCheckBoxMenuItem;
    private javax.swing.JCheckBoxMenuItem ShowSentencesInSeparateWinMenuItem;
    private javax.swing.JTextPane ShowSentencesTextPane;
    private javax.swing.JMenu SortMenuItem;
    private javax.swing.JTextField StatusBarTextField;
    private javax.swing.JList TermsFavList;
    private javax.swing.JList TermsList;
    private javax.swing.JPanel TermsPanel;
    private javax.swing.JTree TermsTree;
    private javax.swing.JCheckBoxMenuItem TermsTreeCheckBoxMenuItem;
    private javax.swing.JPanel TermsTreePanel;
    private javax.swing.JMenuItem Wcount1MenuItem;
    private javax.swing.JMenuItem WcountMenuItem;
    private javax.swing.JMenuItem alphabeticallyMenuItem;
    private javax.swing.JButton buttonAllCollectionMongoBD;
    private javax.swing.JButton buttonLigCorpusCollectionMongoBD;
    private javax.swing.JButton buttonOntologiesCollectionMongoBD;
    private javax.swing.JPanel gridPanel;
    private javax.swing.JComboBox jComboBox1;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JMenu jMenu1;
    private javax.swing.JMenu jMenu2;
    private javax.swing.JMenuItem jMenuItem1;
    private javax.swing.JMenuItem jMenuItem2;
    private javax.swing.JMenuItem jMenuItem3;
    private javax.swing.JMenuItem jMenuItem4;
    private javax.swing.JMenuItem jMenuItem5;
    private javax.swing.JMenuItem jMenuItem6;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JScrollPane jScrollPane4;
    private javax.swing.JScrollPane jScrollPane5;
    private javax.swing.JScrollPane jScrollPane6;
    private javax.swing.JScrollPane jScrollPane7;
    private javax.swing.JScrollPane jScrollPane8;
    private javax.swing.JTextArea jTextArea1;
    private javax.swing.JList listMongoDB;
    private DefaultListModel listModelMongoDB;
    private javax.swing.JPopupMenu popupMenu;
    private JMenuItem menuItemAdd;
    private JMenuItem menuItemAddColumn;
    private JMenuItem menuItemRemoveCurrentColumn;
    private JMenuItem menuItemRemove;
    private JMenuItem menuItemRemoveAll;
    private JMenuItem menuItemCopySelectedCell;
    private JMenuItem menuItemPasteSelectedCell;
    private JMenuItem menuItemSaveToFile;
    private String selectedCellForCopy;
    private javax.swing.JTable tableConfor;
    private javax.swing.table.DefaultTableModel tableModel;
    // End of variables declaration//GEN-END:variables
}
