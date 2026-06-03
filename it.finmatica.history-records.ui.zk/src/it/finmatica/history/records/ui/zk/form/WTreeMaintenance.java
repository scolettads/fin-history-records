/***********************************************************************
 * This file is part of iDempiere ERP Open Source                      *
 * http://www.idempiere.org                                            *
 *                                                                     *
 * Copyright (C) Contributors                                          *
 *                                                                     *
 * This program is free software; you can redistribute it and/or       *
 * modify it under the terms of the GNU General Public License         *
 * as published by the Free Software Foundation; either version 2      *
 * of the License, or (at your option) any later version.              *
 *                                                                     *
 * This program is distributed in the hope that it will be useful,     *
 * but WITHOUT ANY WARRANTY; without even the implied warranty of      *
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the        *
 * GNU General Public License for more details.                        *
 *                                                                     *
 * You should have received a copy of the GNU General Public License   *
 * along with this program; if not, write to the Free Software         *
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,          *
 * MA 02110-1301, USA.                                                 *
 *                                                                     *
 * Author:       s.coletta@ads.it                                      *
 * Company:      Finmatica S.p.A.                                      *
 * Organization: Associazione ERP Open Source Italia                   *
 **********************************************************************/
package it.finmatica.history.records.ui.zk.form;

import java.util.ArrayList;
import java.util.logging.Level;

import org.adempiere.util.Callback;
import org.adempiere.webui.LayoutUtils;
import org.adempiere.webui.apps.ProcessModalDialog;
import org.adempiere.webui.component.Button;
import org.adempiere.webui.component.Checkbox;
import org.adempiere.webui.component.Grid;
import org.adempiere.webui.component.GridFactory;
import org.adempiere.webui.component.Label;
import org.adempiere.webui.component.Listbox;
import org.adempiere.webui.component.ListboxFactory;
import org.adempiere.webui.component.Mask;
import org.adempiere.webui.component.Panel;
import org.adempiere.webui.component.Row;
import org.adempiere.webui.component.Rows;
import org.adempiere.webui.component.Searchbox;
import org.adempiere.webui.component.SimpleListModel;
import org.adempiere.webui.component.SimpleTreeModel;
import org.adempiere.webui.component.Window;
import org.adempiere.webui.panel.ADForm;
import org.adempiere.webui.panel.CustomForm;
import org.adempiere.webui.panel.IFormController;
import org.adempiere.webui.session.SessionManager;
import org.adempiere.webui.theme.ThemeManager;
import org.adempiere.webui.util.ZKUpdateUtil;
// scoletta@ads.it - History Records Plugin
// vanilla 13: FDialog è stato rinominato Dialog (stessa API statica: error/warn/info/ask).
import org.adempiere.webui.window.Dialog;
import org.compiere.model.MProcess;
import org.compiere.model.MTree;
import org.compiere.model.MTreeNode;
import org.compiere.process.ProcessInfoParameter;
import org.compiere.util.Env;
import org.compiere.util.KeyNamePair;
import org.compiere.util.Msg;
import org.compiere.util.Util;
import org.zkoss.zk.au.out.AuScript;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.util.Clients;
import org.zkoss.zul.Borderlayout;
import org.zkoss.zul.Center;
import org.zkoss.zul.DefaultTreeNode;
import org.zkoss.zul.Div;
import org.zkoss.zul.East;
import org.zkoss.zul.ListModel;
import org.zkoss.zul.North;
import org.zkoss.zul.Space;
import org.zkoss.zul.Splitter;
import org.zkoss.zul.Tree;
import org.zkoss.zul.TreeModel;
import org.zkoss.zul.TreeNode;
import org.zkoss.zul.Treeitem;

import it.finmatica.history.records.ui.zk.internal.HSTTreeMaintenance;

/**
 * Tree Maintenance - ZK UI
 *
 * @author Jorg Janke (modify: Sergio Oropeza, Finmatica)
 */
public class WTreeMaintenance extends HSTTreeMaintenance implements IFormController, EventListener<Event> {

	private CustomForm form = new CustomForm();

	private Borderlayout mainLayout = new Borderlayout();
	private Panel northPanel = new Panel();
	private Label treeLabel = new Label();
	private Listbox treeField = ListboxFactory.newDropdownListbox();
	private Button bAddAll = new Button();
	private Button bAdd = new Button();
	private Button bDelete = new Button();
	private Button bDeleteAll = new Button();
	private Checkbox cbAllNodes = new Checkbox();
	private Label treeInfo = new Label();
	private Searchbox searchBox = new Searchbox();
	@SuppressWarnings("unused")
	private Splitter splitPane = new Splitter();
	private Tree centerTree;
	private Listbox centerList = new Listbox();

	// HST
	private Label versionLabel = new Label();
	private Listbox versionField = ListboxFactory.newDropdownListbox();
	private Button bNewTree = new Button();
	private static final String NEW_TREE_PROCESS = "HST_CreateNewTree";
	private Grid headerGrid = GridFactory.newGridLayout();

	public WTreeMaintenance() {
		try {
			m_WindowNo = form.getWindowNo();
			preInit();
			jbInit();
			action_loadTree();
			// scoletta@ads.it - History Records Plugin
			// vanilla 13: LayoutUtils.sendDeferLayoutEvent non esiste (era patch Finmatica 8.2, già deprecated/no-op).
		} catch (Exception ex) {
			log.log(Level.SEVERE, "VTreeMaintenance.init", ex);
		}
	}

	private void preInit() {
		versionField.addActionListener(this);
		treeField.addActionListener(this);
		loadVersions(false, 0);
		centerTree = new Tree();
		centerTree.addEventListener(Events.ON_SELECT, this);
	}

	private void loadVersions(boolean reloadTreeView, int versionID) {
		loadListVersions(versionID, versionField, 0);
		loadTrees(reloadTreeView, 0);
	}

	public static void loadListVersions(int versionID, Listbox field, int defaultADTreeID) {
		field.removeAllItems();
		KeyNamePair[] versions = getVersionTreeData(defaultADTreeID);
		for (KeyNamePair v : versions)
			field.appendItem(v.getName(), v);

		int index = -1;
		if (versionID > 0) {
			for (int i = 0; i < field.getItemCount(); i++) {
				KeyNamePair knp = field.getItemAtIndex(i).getValue();
				if (knp.getKey() == versionID) {
					index = i;
					break;
				}
			}
		}
		if (index >= 0)
			field.setSelectedIndex(index);
	}

	public static void loadListTrees(int treeID, Listbox field, int versionID) {
		field.removeAllItems();
		KeyNamePair[] trees = getTreeData(versionID);
		for (KeyNamePair t : trees)
			field.appendItem(t.getName(), t);

		int index = 0;
		if (field.getItemCount() > 1) {
			if (treeID > 0) {
				for (int i = 0; i < field.getItemCount(); i++) {
					KeyNamePair knp = field.getItemAtIndex(i).getValue();
					if (knp.getKey() == treeID) {
						index = i;
						break;
					}
				}
			} else {
				int activeID = getCurrenActiveTreeDataID(versionID);
				if (activeID > 0) {
					for (int i = 0; i < field.getItemCount(); i++) {
						KeyNamePair knp = field.getItemAtIndex(i).getValue();
						if (knp.getKey() == activeID) {
							index = i;
							break;
						}
					}
				}
			}
		}
		field.setSelectedIndex(index);
	}

	private void loadTrees(boolean reloadTreeView, int treeID) {
		KeyNamePair tree = ((KeyNamePair) versionField.getSelectedItem().getValue());
		log.info("Version=" + tree);
		loadListTrees(treeID, treeField, tree.getKey());
		if (reloadTreeView) {
			action_loadTree();
			// scoletta@ads.it - History Records Plugin
			// vanilla 13: LayoutUtils.sendDeferLayoutEvent non esiste.
		}
	}

	private void jbInit() throws Exception {
		bAddAll.setImage(ThemeManager.getThemeResource("images/FastBack24.png"));
		bAdd.setImage(ThemeManager.getThemeResource("images/StepBack24.png"));
		bDelete.setImage(ThemeManager.getThemeResource("images/StepForward24.png"));
		bDeleteAll.setImage(ThemeManager.getThemeResource("images/FastForward24.png"));

		ZKUpdateUtil.setWidth(form, "99%");
		ZKUpdateUtil.setHeight(form, "100%");
		form.setStyle("position: absolute; padding: 0; margin: 0");
		form.appendChild(mainLayout);
		ZKUpdateUtil.setWidth(mainLayout, "100%");
		ZKUpdateUtil.setHeight(mainLayout, "100%");
		mainLayout.setStyle("position: absolute");

		versionLabel.setText(Msg.translate(Env.getCtx(), "AD_Tree_ID"));
		bNewTree.setImage(ThemeManager.getThemeResource("images/Process16.png"));
		bNewTree.setTooltiptext(Msg.getMsg(Env.getCtx(), MSG_CREATE_NEW_TREE));
		bNewTree.addActionListener(this);
		treeLabel.setText(Msg.translate(Env.getCtx(), "HST_History"));

		cbAllNodes.setEnabled(false);
		cbAllNodes.setText(Msg.translate(Env.getCtx(), "IsAllNodes"));
		treeInfo.setText(" ");
		bAdd.setTooltiptext(Msg.getMsg(Env.getCtx(), "AddToTree"));
		bAddAll.setTooltiptext(Msg.getMsg(Env.getCtx(), "AddAllToTree"));
		bDelete.setTooltiptext(Msg.getMsg(Env.getCtx(), "DeleteFromTree"));
		bDeleteAll.setTooltiptext(Msg.getMsg(Env.getCtx(), "DeleteAllFromTree"));
		bAdd.addActionListener(this);
		bAddAll.addActionListener(this);
		bDelete.addActionListener(this);
		bDeleteAll.addActionListener(this);

		North north = new North();
		mainLayout.appendChild(north);
		north.appendChild(northPanel);
		ZKUpdateUtil.setHflex(north, "1");
		ZKUpdateUtil.setVflex(north, "1");
		ZKUpdateUtil.setWidth(northPanel, "100%");
		ZKUpdateUtil.setHeight(northPanel, "100%");

		Div div = new Div();
		div.appendChild(bAddAll);
		div.appendChild(bAdd);
		div.appendChild(bDelete);
		div.appendChild(bDeleteAll);

		searchBox.addEventListener(Events.ON_CLICK, this);
		searchBox.getTextbox().addEventListener(Events.ON_OK, this);
		searchBox.getButton().setImage(ThemeManager.getThemeResource("images/Find16.png"));
		searchBox.setToolTipText(Msg.getCleanMsg(Env.getCtx(), "TreeSearch"));
		ZKUpdateUtil.setWidth(searchBox, "200px");

		northPanel.appendChild(headerGrid);
		Rows rows = headerGrid.newRows();
		Row row = rows.newRow();
		row.appendCellChild(versionLabel, 1);
		row.appendCellChild(versionField, 5);
		row.appendCellChild(bNewTree, 2);
		row.appendCellChild(new Space(), 4);
		row.appendCellChild(new Space(), 3);

		row = rows.newRow();
		row.appendCellChild(treeLabel, 1);
		row.appendCellChild(treeField, 5);
		row.appendCellChild(cbAllNodes, 2);
		row.appendCellChild(div, 4);
		row.appendCellChild(searchBox, 3);

		Center center = new Center();
		mainLayout.appendChild(center);
		center.appendChild(centerTree);
		ZKUpdateUtil.setVflex(centerTree, "1");
		ZKUpdateUtil.setHflex(centerTree, "1");
		center.setAutoscroll(true);

		East east = new East();
		mainLayout.appendChild(east);
		east.appendChild(centerList);
		east.setCollapsible(false);
		east.setSplittable(true);
		ZKUpdateUtil.setWidth(east, "45%");
		ZKUpdateUtil.setVflex(centerList, true);
		centerList.setSizedByContent(false);
		centerList.addEventListener(Events.ON_SELECT, this);
	}

	public void dispose() {
		SessionManager.getAppDesktop().closeActiveWindow();
	}

	public void onEvent(Event e) {
		if (e.getTarget() == versionField) {
			loadTrees(true, 0);
		}
		if (e.getTarget() == treeField) {
			action_loadTree();
			// scoletta@ads.it - History Records Plugin
			// vanilla 13: LayoutUtils.sendDeferLayoutEvent non esiste.
		} else if (e.getTarget() == bAddAll)
			action_treeAddAll();
		else if (e.getTarget() == bAdd) {
			SimpleListModel model = (SimpleListModel) centerList.getModel();
			int i = centerList.getSelectedIndex();
			if (i >= 0) {
				action_treeAdd((ListItem) model.getElementAt(i));
			}
		} else if (e.getTarget() == bDelete) {
			SimpleListModel model = (SimpleListModel) centerList.getModel();
			int i = centerList.getSelectedIndex();
			if (i >= 0) {
				action_treeDelete((ListItem) model.getElementAt(i));
			}
		} else if (e.getTarget() == bDeleteAll)
			action_treeDeleteAll();
		else if (e.getTarget() == centerList)
			onListSelection(e);
		else if (e.getTarget() == centerTree)
			onTreeSelection(e);
		else if (e.getTarget() == searchBox.getButton() || e.getTarget() == searchBox.getTextbox())
			searchElement();
		else if (e.getTarget() == bNewTree)
			executeButtonProcess();
		else if (e.getTarget() instanceof ProcessModalDialog && e.getName().equals(ProcessModalDialog.ON_WINDOW_CLOSE))
		{
			hideBusyMask();
			ProcessModalDialog dialog = (ProcessModalDialog) e.getTarget();
			String msg = dialog.getProcessInfo().getSummary();
			if (!Util.isEmpty(msg)) {
				int newTreeID = 0;
				try {
					newTreeID = Integer.valueOf(msg);
				} catch (Exception ex) {
				}
				if (newTreeID > 0) {
					ProcessInfoParameter[] para = dialog.getProcessInfo().getParameter();
					boolean p_IsNewVersion = false;
					for (int i = 0; i < para.length; i++) {
						String name = para[i].getParameterName();
						if (name.equals("IsNewVersion")) {
							p_IsNewVersion = "Y".equals(para[i].getParameter());
							break;
						}
					}
					if (p_IsNewVersion) {
						loadVersions(true, newTreeID);
					} else {
						loadTrees(true, newTreeID);
					}
				} else {
					Dialog.error(m_WindowNo, msg);
				}
			}
		}
	}

	private void executeButtonProcess() {
		int processID = MProcess.getProcess_ID(NEW_TREE_PROCESS, null);
		int recordID = ((KeyNamePair) treeField.getSelectedItem().getValue()).getKey();
		// scoletta@ads.it - History Records Plugin
		// vanilla 13: ProcessModalDialog richiede 7 parametri; aggiunto null per recordUU
		ProcessModalDialog dialog = new ProcessModalDialog(this, m_WindowNo, processID, MTree.Table_ID, recordID,
				null, false);
		if (dialog.isValid()) {
			dialog.setBorder("normal");
			mainLayout.getParent().appendChild(dialog);
			showBusyMask(dialog);
			LayoutUtils.openOverlappedWindow(mainLayout.getParent(), dialog, "middle_center");
			dialog.focus();
		}
	}

	private void showBusyMask(Window window) {
		mainLayout.getParent().appendChild(getMask());
		StringBuilder script = new StringBuilder("var w=zk.Widget.$('#");
		script.append(mainLayout.getParent().getUuid()).append("');");
		if (window != null) {
			script.append("var d=zk.Widget.$('#").append(window.getUuid()).append("');w.busy=d;");
		} else {
			script.append("w.busy=true;");
		}
		Clients.response(new AuScript(script.toString()));
	}

	private void hideBusyMask() {
		if (mask != null && mask.getParent() != null) {
			mask.detach();
			StringBuilder script = new StringBuilder("var w=zk.Widget.$('#");
			script.append(mainLayout.getParent().getUuid()).append("');if(w) w.busy=false;");
			Clients.response(new AuScript(script.toString()));
		}
	}

	private Div mask;

	private Div getMask() {
		if (mask == null) {
			mask = new Mask();
		}
		return mask;
	}

	private void searchElement() {
		String filter = searchBox.getText() == null ? "" : searchBox.getText();
		filter = Util.deleteAccents(filter.trim().toUpperCase());
		action_loadTree(filter);
	}

	private void action_loadTree() {
		action_loadTree(null);
	}

	private void action_loadTree(String filter) {
		KeyNamePair tree = ((KeyNamePair) treeField.getSelectedItem().getValue());
		log.info("Tree=" + tree);
		if (tree.getKey() <= 0) {
			SimpleListModel tmp = new SimpleListModel();
			centerList.setItemRenderer(tmp);
			centerList.setModel(tmp);
			return;
		}
		m_tree = new MTree(Env.getCtx(), tree.getKey(), null);
		cbAllNodes.setSelected(m_tree.isAllNodes());
		bAddAll.setEnabled(!m_tree.isAllNodes());
		bAdd.setEnabled(!m_tree.isAllNodes());
		bDelete.setEnabled(!m_tree.isAllNodes());
		bDeleteAll.setEnabled(!m_tree.isAllNodes());

		SimpleListModel model = new SimpleListModel();
		ArrayList<ListItem> items = getTreeItemData();
		for (ListItem item : items) {
			if (Util.isEmpty(filter)) {
				model.addElement(item);
			} else {
				String valueItem = item.toString() == null ? "" : Util.deleteAccents(item.toString().toUpperCase());
				if (valueItem.contains(filter)) {
					model.addElement(item);
				}
			}
		}
		if (log.isLoggable(Level.CONFIG))
			log.config("#" + model.getSize());
		centerList.setItemRenderer(model);
		centerList.setModel(model);

		try {
			centerTree.setModel(null);
		} catch (Exception e) {
		}
		if (centerTree.getTreecols() != null)
			centerTree.getTreecols().detach();
		if (centerTree.getTreefoot() != null)
			centerTree.getTreefoot().detach();
		if (centerTree.getTreechildren() != null)
			centerTree.getTreechildren().detach();

		SimpleTreeModel.initADTree(centerTree, m_tree.getAD_Tree_ID(), m_WindowNo);
	}

	private void onListSelection(Event e) {
		ListItem selected = null;
		try {
			SimpleListModel model = (SimpleListModel) centerList.getModel();
			int i = centerList.getSelectedIndex();
			selected = (ListItem) model.getElementAt(i);
		} catch (Exception ex) {
		}
		log.info("Selected=" + selected);
		if (selected != null) {
			SimpleTreeModel tm = (SimpleTreeModel) (TreeModel<?>) centerTree.getModel();
			DefaultTreeNode<Object> stn = tm.find(tm.getRoot(), selected.id);
			if (stn != null) {
				int[] path = tm.getPath(stn);
				Treeitem ti = centerTree.renderItemByPath(path);
				ti.setSelected(true);
			}
			bAdd.setEnabled(stn == null);
		}
	}

	private void onTreeSelection(Event e) {
		Treeitem ti = centerTree.getSelectedItem();
		DefaultTreeNode<?> stn = (DefaultTreeNode<?>) ti.getValue();
		MTreeNode tn = (MTreeNode) stn.getData();
		if (tn == null)
			return;
		log.info(tn.toString());
		ListModel<Object> model = centerList.getModel();
		int size = model.getSize();
		int found = -1;
		for (int index = 0; index < size; index++) {
			ListItem item = (ListItem) model.getElementAt(index);
			if (item.id == tn.getNode_ID()) {
				found = index;
				break;
			}
		}
		centerList.setSelectedIndex(found);
	}

	private void action_treeAdd(ListItem item) {
		log.info("Item=" + item);
		if (item != null) {
			SimpleTreeModel model = (SimpleTreeModel) (TreeModel<?>) centerTree.getModel();
			DefaultTreeNode<Object> stn = model.find(model.getRoot(), item.id);
			if (stn != null) {
				MTreeNode tNode = (MTreeNode) stn.getData();
				tNode.setName(item.name);
				tNode.setAllowsChildren(item.isSummary);
				tNode.setImageIndicator(item.imageIndicator);
				model.nodeUpdated(stn);
				Treeitem ti = centerTree.renderItemByPath(model.getPath(stn));
				ti.setTooltiptext(item.description);
			} else {
				stn = new DefaultTreeNode<Object>(new MTreeNode(item.id, 0, item.name, item.description, 0,
						item.isSummary, item.imageIndicator, false, null), new ArrayList<TreeNode<Object>>());
				model.addNode(stn);
			}
			addNode(item);
		}
	}

	private void action_treeDelete(ListItem item) {
		log.info("Item=" + item);
		if (item != null) {
			SimpleTreeModel model = (SimpleTreeModel) (TreeModel<?>) centerTree.getModel();
			DefaultTreeNode<Object> stn = model.find(model.getRoot(), item.id);
			if (stn != null)
				model.removeNode(stn);
			deleteNode(item);
		}
	}

	private void action_treeAddAll() {
		Dialog.ask(m_WindowNo, null, "TreeAddAllItems", new Callback<Boolean>() {
			@Override
			public void onCallback(Boolean result) {
				if (result) {
					log.info("");
					ListModel<Object> model = centerList.getModel();
					int size = model.getSize();
					for (int index = 0; index < size; index++) {
						ListItem item = (ListItem) model.getElementAt(index);
						action_treeAdd(item);
					}
				}
			}
		});
	}

	private void action_treeDeleteAll() {
		log.info("");
		Dialog.ask(m_WindowNo, null, "TreeRemoveAllItems", new Callback<Boolean>() {
			@Override
			public void onCallback(Boolean result) {
				if (result) {
					ListModel<Object> model = centerList.getModel();
					int size = model.getSize();
					for (int index = 0; index < size; index++) {
						ListItem item = (ListItem) model.getElementAt(index);
						action_treeDelete(item);
					}
				}
			}
		});
	}

	public ADForm getForm() {
		return form;
	}
}
