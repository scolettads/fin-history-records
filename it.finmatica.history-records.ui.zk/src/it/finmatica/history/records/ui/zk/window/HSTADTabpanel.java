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
// scoletta@ads.it - History Records Plugin
// Adattato per vanilla iDempiere 13:
// - generateWestPanel(int AD_Tree_ID) non esiste → logica spostata in createUI() post-super,
//   tramite setupWestPanel() privato. West esistente da ADTabpanel.init() viene ristrutturato.
// - windowPanel (private in ADTabpanel) → getADWindowContent().
// - ProcessModalDialog: aggiunto null per recordUU (7° parametro).
// - node.setFIN_Level(0) → node.set_Value("FIN_Level", 0) (colonna Finmatica non presente).
// - getTreeNodeFromModel() non esiste in ADTabpanel vanilla 13 → reimplementato localmente.
// - navigateTo() è private in ADTabpanel vanilla 13 → navigateToNode() locale.
// - isTreeDrivenByValue() è private in ADTabpanel vanilla 13 → isTreeDrivenByValueLocal().
// - getADTreeIDForRefresh() non esiste in ADTabpanel vanilla 13 → rimosso @Override.
// - ON_DEFER_SET_SELECTED_NODE/ATTR: private in ADTabpanel → ridichiarati con stesso valore.
// - isEditable(): rimossi check Finmatica-specifici (FINCE_BUDGET, FIN_CONTRACT non presenti).
// - HSTTreeMaintenance: package cambiato da ui.form a ui.zk.internal.
package it.finmatica.history.records.ui.zk.window;

import org.adempiere.webui.LayoutUtils;
import org.adempiere.webui.adwindow.ADTabpanel;
import org.adempiere.webui.adwindow.ADTreePanel;
import org.adempiere.webui.adwindow.DetailPane;
import org.adempiere.webui.apps.ProcessModalDialog;
import org.adempiere.webui.component.Borderlayout;
import org.adempiere.webui.component.Button;
import org.adempiere.webui.component.Grid;
import org.adempiere.webui.component.GridFactory;
import org.adempiere.webui.component.Label;
import org.adempiere.webui.component.Listbox;
import org.adempiere.webui.component.ListboxFactory;
import org.adempiere.webui.component.Panel;
import org.adempiere.webui.component.Row;
import org.adempiere.webui.component.Rows;
import org.adempiere.webui.component.SimpleTreeModel;
import org.adempiere.webui.panel.TreeSearchPanel;
import org.adempiere.webui.theme.ThemeManager;
import org.adempiere.webui.util.ZKUpdateUtil;
// scoletta@ads.it - History Records Plugin
// vanilla 13: FDialog è stato rinominato Dialog (stessa API statica: error/warn/info/ask).
import org.adempiere.webui.window.Dialog;
import org.compiere.model.DataStatusEvent;
import org.compiere.model.MProcess;
import org.compiere.model.MQuery;
import org.compiere.model.MRole;
import org.compiere.model.MTree;
import org.compiere.model.MTreeNode;
import org.compiere.model.MTree_Node;
import org.compiere.model.MTree_NodeBP;
import org.compiere.model.MTree_NodeMM;
import org.compiere.model.MTree_NodePR;
import org.compiere.model.MTable;
import org.compiere.model.PO;
import org.compiere.process.ProcessInfoParameter;
import org.compiere.util.DB;
import org.compiere.util.Env;
import org.compiere.util.KeyNamePair;
import org.compiere.util.Msg;
import org.compiere.util.Util;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zul.Center;
import org.zkoss.zul.DefaultTreeNode;
import org.zkoss.zul.North;
import org.zkoss.zul.Tree;
import org.zkoss.zul.TreeModel;
import org.zkoss.zul.TreeNode;
import org.zkoss.zul.Treeitem;
import org.zkoss.zul.West;

import it.finmatica.history.records.ui.zk.form.WTreeMaintenance;
import it.finmatica.history.records.ui.zk.internal.HSTTreeMaintenance;

public class HSTADTabpanel extends ADTabpanel {

	private static final long serialVersionUID = -3728896318124756192L;

	// scoletta@ads.it - History Records Plugin
	// ON_DEFER_SET_SELECTED_NODE e ON_DEFER_SET_SELECTED_NODE_ATTR sono private in
	// ADTabpanel vanilla 13; ridichiarati qui con gli stessi valori letterali.
	private static final String ON_DEFER_SET_SELECTED_NODE      = "onDeferSetSelectedNode";
	private static final String ON_DEFER_SET_SELECTED_NODE_ATTR = "onDeferSetSelectedNode.Event.Posted";

	private Label   versionLabel = new Label();
	private Listbox versionField = ListboxFactory.newDropdownListbox();
	private Label   treeLabel    = new Label();
	private Listbox treeField    = ListboxFactory.newDropdownListbox();
	private Button  bNewTree     = new Button();

	private Button bFilter  = new Button();
	private Button bAdd     = new Button();
	private Button bDelete  = new Button();

	/*
	 * fin acg: nuovi bottoni
	 */
	private Button bSelRight = new Button();
	private Button bSelLeft  = new Button();

	private static final String NEW_TREE_PROCESS    = "HST_CreateNewTree";
	private static final String MSG_TREE_IS_ALLNODES = "HST_ERR_TREE_IS_ALLNODES";
	private static final String MSG_NO_REC_SELECTED  = "HST_WARN_NO_REC_SELECTED";

	private Grid  headerGrid    = GridFactory.newGridLayout();
	private MTree m_tree;
	private int   defaultADTreeID;
	private boolean filterApplied = false;

	private int currentSelectedTreeID = 0;

	/*
	 * fin acg: accesso a variabili private
	 */
	public boolean isFilterApplied()
	{
		return filterApplied;
	}

	public void setFilterApplied(boolean filterApplied)
	{
		this.filterApplied = filterApplied;
	}

	public Listbox getVersionField() { return versionField; }
	public Listbox getTreeField()    { return treeField;    }
	public Button  getbNewTree()     { return bNewTree;     }
	public Button  getbFilter()      { return bFilter;      }
	public Button  getbAdd()         { return bAdd;         }
	public Button  getbDelete()      { return bDelete;      }
	public Button  getbSelRight()    { return bSelRight;    }
	public Button  getbSelLeft()     { return bSelLeft;     }

	public HSTADTabpanel()
	{
		super();
	}

	/**
	 * scoletta@ads.it - History Records Plugin
	 * In vanilla iDempiere 13 ADTabpanel.generateWestPanel(int) non esiste.
	 * La logica di setup del West panel (header con bottoni + treePanel in sideLayout)
	 * viene eseguita qui in createUI() dopo super.createUI(), che crea il West vanilla
	 * con solo treePanel. setupWestPanel() rimuove treePanel dal West e ricrea la
	 * struttura sideLayout (North=header + Center=treePanel).
	 */
	@Override
	public void createUI()
	{
		// 1. Compute defaultADTreeID (era il parametro di generateWestPanel in 8.2).
		if (getGridTab().getTabLevel() == 0)
		{
			defaultADTreeID = MTree.getDefaultAD_Tree_ID(
					Env.getAD_Client_ID(Env.getCtx()), getGridTab().getKeyColumnName());
		}
		else
		{
			defaultADTreeID = getParentTabTreeID();
		}

		// 2. Build form fields + init tree con comportamento standard vanilla.
		super.createUI();

		// 3. Restructure West panel solo se è un tab con albero.
		if (getGridTab().isTreeTab() && getTreePanel() != null)
		{
			setupWestPanel();
		}

		// 4. Disabilita drag-and-drop per ruoli in sola lettura (era nel createUI di 8.2).
		MRole role = MRole.get(Env.getCtx(), Env.getAD_Role_ID(Env.getCtx()));
		Boolean isWindowAccess = role.getWindowAccess(getGridTab().getAD_Window_ID());
		if (isWindowAccess == null || !isWindowAccess)
		{
			ADTreePanel treePanel = getTreePanel();
			if (treePanel != null)
			{
				Tree tree = treePanel.getTree();
				if (tree != null)
				{
					Object objModel = tree.getModel();
					if (objModel != null)
					{
						SimpleTreeModel model = (SimpleTreeModel) (TreeModel<?>) objModel;
						model.setItemDraggable(false);
					}
				}
			}
		}
	}

	/**
	 * scoletta@ads.it - History Records Plugin
	 * Costruisce l'intestazione (header con bottoni versione/albero) nel West panel
	 * esistente. In 8.2 era inline in generateWestPanel; in vanilla 13 viene chiamato
	 * da createUI() dopo super.createUI(). Il West creato da ADTabpanel.init() contiene
	 * già solo treePanel; qui si rimuove treePanel, si costruisce sideLayout
	 * (North=header + Center=treePanel) e lo si reinserisce nel West.
	 */
	private void setupWestPanel()
	{
		bNewTree.setImage(ThemeManager.getThemeResource("images/Process16.png"));
		bNewTree.setStyle(" padding: 4px 4px; ");
		bNewTree.setTooltiptext(Msg.getMsg(Env.getCtx(), HSTTreeMaintenance.MSG_CREATE_NEW_TREE));
		bNewTree.addActionListener(this);
		//
		bAdd.setImage(ThemeManager.getThemeResource("images/Parent16.png"));
		bAdd.setStyle(" padding: 4px 4px; ");
		bAdd.setTooltiptext(Msg.getMsg(Env.getCtx(), HSTTreeMaintenance.MSG_ADD_TO_TREE));
		bAdd.addActionListener(this);
		//
		bFilter.setImage(ThemeManager.getThemeResource("images/Find16.png"));
		bFilter.setStyle(" padding: 4px 4px; ");
		bFilter.setTooltiptext(Msg.getMsg(Env.getCtx(), HSTTreeMaintenance.MSG_FILTER_TREE));
		bFilter.addActionListener(this);
		//
		bDelete.setImage(ThemeManager.getThemeResource("images/Cancel16.png"));
		bDelete.setStyle(" padding: 4px 4px; ");
		bDelete.setTooltiptext(Msg.getMsg(Env.getCtx(), HSTTreeMaintenance.MSG_REMOVE_FROM_TREE));
		bDelete.addActionListener(this);

		/*
		 * fin acg: nuovi bottoni
		 */
		bSelRight.setImage(ThemeManager.getThemeResource("images/tree16.png"));
		bSelRight.setStyle(" padding: 4px 4px; ");
		bSelRight.setTooltiptext("Visualizza dettaglio");
		bSelRight.addActionListener(this);

		bSelLeft.setImage(ThemeManager.getThemeResource("images/det16.png"));
		bSelLeft.setStyle(" padding: 4px 4px; ");
		bSelLeft.setTooltiptext("Cerca nell'albero");
		bSelLeft.addActionListener(this);

		versionLabel.setText(Msg.translate(Env.getCtx(), "AD_Tree_ID"));
		treeLabel.setText(Msg.translate(Env.getCtx(), "HST_History"));

		//SN: disabilito i pulsanti se ho l'accesso in sola lettura
		MRole role = MRole.get(Env.getCtx(), Env.getAD_Role_ID(Env.getCtx()));
		Boolean isWindowAccess = role.getWindowAccess(getGridTab().getAD_Window_ID());
		if (isWindowAccess == null || !isWindowAccess)
		{
			bNewTree.setDisabled(true);
			bDelete.setDisabled(true);
			bAdd.setDisabled(true);
		}
		//

		Rows rows = headerGrid.newRows();
		Row row = rows.newRow();
		row.appendCellChild(versionLabel, 2);
		row.appendCellChild(versionField, 6);
		row.appendCellChild(bNewTree, 1);
		row.appendCellChild(bDelete, 1);
		/*
		 * fin acg: add nuovo bottone
		 */
		row.appendCellChild(bSelRight);

		row = rows.newRow();
		row.appendCellChild(treeLabel, 2);
		row.appendCellChild(treeField, 6);
		row.appendCellChild(bFilter, 1);
		row.appendCellChild(bAdd, 1);
		/*
		 * fin acg: add nuovo bottone
		 */
		row.appendCellChild(bSelLeft);

		ZKUpdateUtil.setWidth(versionField, "100%");
		ZKUpdateUtil.setWidth(treeField, "100%");

		// Vanilla 13: il West esiste già (creato da ADTabpanel.init()) con solo treePanel.
		// Ristrutturiamo: rimuoviamo treePanel dal West e inseriamo sideLayout.
		West west = (West) getTreePanel().getParent();
		getTreePanel().detach();

		Borderlayout sideLayout = new Borderlayout();

		North north = new North();
		Panel northPanel = new Panel();
		north.appendChild(northPanel);
		northPanel.appendChild(headerGrid);
		ZKUpdateUtil.setWidth(northPanel, "100%");
		ZKUpdateUtil.setHeight(northPanel, "100%");
		sideLayout.appendChild(north);

		Center center = new Center();
		center.appendChild(getTreePanel());
		center.setAutoscroll(true);
		sideLayout.appendChild(center);

		west.appendChild(sideLayout);
		ZKUpdateUtil.setWidth(west, "300px");

		versionField.addActionListener(this);
		treeField.addActionListener(this);

		loadVersions(false, defaultADTreeID);
	}

	/*
	 * fin acg
	 * override di query per convivenza filtri (filteronlymissing e filtri da toolbar)
	 */
	@Override
	public void query(boolean onlyCurrentRows, int onlyCurrentDays, int maxRows)
	{
		if (filterApplied && !isDetailPaneMode())
			filterOnlyMissing();
		else
			super.query(onlyCurrentRows, onlyCurrentDays, maxRows);
	}

	@Override
	public void onEvent(Event e)
	{
		if (e.getTarget() == versionField)
		{
			loadTrees(true, 0);
			filterApplied = false;
			filterOnlyMissing();
		}
		else if (e.getTarget() == treeField)
		{
			udateSelectedTree(true);
			filterApplied = false;
			filterOnlyMissing();
		}
		else if (e.getTarget() == bNewTree)
		{
			if (!isDetailPaneMode())
			{
				executeButtonProcess();
			}
		}
		else if (e.getTarget() == bDelete)
		{
			//FS Feature #29608
			if (isEditable())
			{
				if (!isDetailPaneMode())
				{
					if (m_tree.isAllNodes())
					{
						Dialog.error(getWindowNo(), Msg.getMsg(Env.getCtx(), MSG_TREE_IS_ALLNODES));
					}
					else
					{
						Treeitem sel = getTreePanel().getTree().getSelectedItem();
						if (sel != null)
						{
							action_treeDelete(sel);
							filterOnlyMissing();
						}
						else
						{
							Dialog.warn(getWindowNo(), Msg.getMsg(Env.getCtx(), MSG_NO_REC_SELECTED));
						}
					}
				}
			}
		}
		else if (e.getTarget() == bAdd)
		{
			if (!isDetailPaneMode())
			{
				Treeitem selectedItem = getTreePanel().getTree().getSelectedItem();
				DefaultTreeNode<Object> selectedNode = null;
				if (selectedItem != null)
				{
					selectedNode = selectedItem.getValue();
				}

				int selectedRecordID = -1;
				if (selectedNode != null)
				{
					MTreeNode selectedRoot = (MTreeNode) selectedNode.getData();
					if (selectedRoot != null)
						selectedRecordID = selectedRoot.getNode_ID();
				}

				final int[] indices = getADWindowContent().getADTab().getSelectedGridTab().getSelection();
				if (indices.length > 0 && getADWindowContent().getADTab().getSelectedTabpanel().isGridView())
				{
					for (Integer i : getADWindowContent().getADTab().getSelectedGridTab().getSelection())
					{
						int recID = getADWindowContent().getADTab().getSelectedGridTab().getKeyID(i.intValue());
						action_treeAdd(recID, selectedRecordID);
					}
					getADWindowContent().getADTab().getSelectedGridTab().clearSelection();
					getADWindowContent().getADTab().getSelectedGridTab().dataRefreshAll(true, true);
				}
				else
				{
					int i = getADWindowContent().getADTab().getSelectedGridTab().getCurrentRow();
					int recID = getADWindowContent().getADTab().getSelectedGridTab().getKeyID(i);
					action_treeAdd(recID, selectedRecordID);
				}
				filterOnlyMissing();
			}
		}
		else if (e.getTarget() == bFilter)
		{
			if (!isDetailPaneMode())
			{
				filterApplied = !filterApplied;
				filterOnlyMissing();
			}

			bSelRight.setDisabled(filterApplied);
			bSelLeft.setDisabled(filterApplied);
		}
		else if (e.getTarget() instanceof ProcessModalDialog
				&& e.getName().equals(ProcessModalDialog.ON_WINDOW_CLOSE))
		{
			getADWindowContent().hideBusyMask();
			ProcessModalDialog dialog = (ProcessModalDialog) e.getTarget();
			String msg = dialog.getProcessInfo().getSummary();
			if (!Util.isEmpty(msg))
			{
				int newTreeID = 0;
				try
				{
					newTreeID = Integer.valueOf(msg);
				}
				catch (Exception ex)
				{
				}

				if (newTreeID > 0)
				{
					ProcessInfoParameter[] para = dialog.getProcessInfo().getParameter();
					boolean p_IsNewVersion = false;
					for (int i = 0; i < para.length; i++)
					{
						String name = para[i].getParameterName();
						if (name.equals("IsNewVersion"))
						{
							p_IsNewVersion = "Y".equals(para[i].getParameter());
							break;
						}
					}
					if (p_IsNewVersion)
					{
						loadVersions(true, newTreeID);
					}
					else
					{
						loadTrees(true, newTreeID);
					}
				}
				else
				{
					Dialog.error(getWindowNo(), msg);
				}
			}
		}
		else if (ON_DEFER_SET_SELECTED_NODE.equals(e.getName()))
		{
			// scoletta@ads.it - History Records Plugin
			// vanilla 13: dopo removeAttribute occorre chiamare setSelectedNode() come fa ADTabpanel.
			removeAttribute(ON_DEFER_SET_SELECTED_NODE_ATTR);
			setSelectedNode();
		}
		else if (getTreePanel() != null && e.getTarget() == getTreePanel().getTree())
		{
			Treeitem item = getTreePanel().getTree().getSelectedItem();
			if (!isDetailPaneMode())
			{
				// when clicking on a node, if filter 'only missing' is set, it's
				// nonsense to navigate to that node because it wouldn't find it

				//fin acg: deve scattare al click del pulsante bSelRight
				/*if (item.getValue() != null && !filterApplied)
				    super.onEvent(e);*/
			}
			else
			{
				//change view mode
				Event openEvent = new Event(DetailPane.ON_EDIT_EVENT, getGridView(), true);
				Events.sendEvent(openEvent);

				//reset filter icon if 'checked'
				if (filterApplied)
				{
					filterApplied = false;
					updateFilterIcon();
				}
				//now navigate to item selected
				if (item != null && item.getValue() != null)
					navigateToNode(item.getValue());
			}
		}
		else if (e.getTarget().equals(bSelRight))
		{
			Treeitem item = getTreePanel().getTree().getSelectedItem();
			if (item != null && item.getValue() != null && !filterApplied)
				super.onEvent(new Event(Events.ON_SELECT, getTreePanel().getTree()));

			getGridView().activate(getGridTab());
		}
		else if (e.getTarget().equals(bSelLeft))
		{
			if (!filterApplied)
			{
				DefaultTreeNode<Object> treeNode = null;
				SimpleTreeModel model = (SimpleTreeModel) (TreeModel<?>) getTreePanel().getTree().getModel();

				int currentRow = getADWindowContent().getADTab().getSelectedGridTab().getCurrentRow();
				if (currentRow >= 0)
				{
					int recID = getADWindowContent().getADTab().getSelectedGridTab().getKeyID(currentRow);
					treeNode = getTreeNodeFromModel(model, recID);
				}

				if (treeNode != null)
				{
					try
					{
						Treeitem treeItem = null;
						DefaultTreeNode<?> sNode = (DefaultTreeNode<?>) treeNode;
						if (sNode != null)
						{
							int[] path = model.getPath((TreeNode<Object>) sNode);
							if (path.length > 0)
							{
								treeItem = getTreePanel().getTree().renderItemByPath(path);
								if (treeItem != null)
								{
									getTreePanel().getTree().setSelectedItem(treeItem);
									TreeSearchPanel.select(treeItem);
								}
							}
						}
					}
					catch (Exception e1)
					{
						System.out.println(e1.toString());
					}
				}
				else
				{
					Dialog.error(getWindowNo(), "Il record selezionato non è presente nell'albero");
				}
			}

		}
		else if (e.getName().equals("onSaveOpenPreference"))
		{
			return;
		}
		else
		{
			super.onEvent(e);
		}
	}


	// scoletta@ads.it - History Records Plugin
	// isEditable(): rimossi check Finmatica-specifici su tabelle FINCE_BUDGET e FIN_CONTRACT
	// (non presenti in vanilla iDempiere 13). Il controllo dell'accesso in sola lettura
	// è già gestito in setupWestPanel(). Restituisce sempre true in vanilla 13.
	private boolean isEditable()
	{
		return true;
	}

	private void filterOnlyMissing()
	{
		//fin acg: fix null pointer se integro il panel in una form
		if (getADWindowContent() != null)
		{
			MQuery backup = getADWindowContent().getADTab().getSelectedGridTab().getQuery().deepCopy();
			int index = getADWindowContent().getADTab().getSelectedGridTab().getCurrentRow();

			if (filterApplied)
			{
				String s = getGridTab().getKeyColumnName() + " not in (Select node_id from "
						+ m_tree.getNodeTableName()
						+ " where ad_tree_id = " + m_tree.get_ID() + ") ";
				getADWindowContent().getADTab().getSelectedGridTab().getQuery().addRestriction(s);
			}

			getADWindowContent().getADTab().getSelectedGridTab().query(false);
			getADWindowContent().getADTab().getSelectedGridTab().setCurrentRow(index);
			getADWindowContent().getADTab().getSelectedGridTab().dataRefreshAll(true, true);

			if (filterApplied)
			{
				getADWindowContent().getADTab().getSelectedGridTab().setQuery(backup);
			}

			updateFilterIcon();
		}
	}

	private void updateFilterIcon()
	{
		if (filterApplied)
			bFilter.setImage(ThemeManager.getThemeResource("images/Find19_sel.png"));
		else
			bFilter.setImage(ThemeManager.getThemeResource("images/Find16.png"));

		bFilter.setTooltiptext(Msg.getMsg(Env.getCtx(),
				(filterApplied ? HSTTreeMaintenance.MSG_REMOVE_FILTER : HSTTreeMaintenance.MSG_FILTER_TREE)));
	}

	private void loadVersions(boolean reloadTreeView, int versionID)
	{
		WTreeMaintenance.loadListVersions(versionID, versionField, defaultADTreeID);
		loadTrees(reloadTreeView, 0);
	}

	private void loadTrees(boolean reloadTreeView, int treeID)
	{
		if (versionField.getSelectedItem() != null)
		{
			KeyNamePair vers = ((KeyNamePair) versionField.getSelectedItem().getValue());
			WTreeMaintenance.loadListTrees(treeID, treeField, vers.getKey());
			udateSelectedTree(reloadTreeView);
		}
	}

	private void udateSelectedTree(boolean reloadTreeView)
	{
		KeyNamePair tree = ((KeyNamePair) treeField.getSelectedItem().getValue());

		currentSelectedTreeID = tree.getKey();
		m_tree = new MTree(Env.getCtx(), tree.getKey(), true, true, null);

		//FS Feature #29608 - Se l'albero e' centralizzato non consento modifiche
		if (m_tree.get_ValueAsInt("FINCA_Tree_ID") > 0)
		{
			bDelete.setDisabled(true);
			bAdd.setDisabled(true);
		}
		else
		{
			bDelete.setDisabled(m_tree.isAllNodes());
			bAdd.setDisabled(m_tree.isAllNodes());
		}

		//SN: disabilito i pulsanti se ho l'accesso in sola lettura
		MRole role = MRole.get(Env.getCtx(), Env.getAD_Role_ID(Env.getCtx()));
		Boolean isWindowAccess = role.getWindowAccess(getGridTab().getAD_Window_ID());
		if (isWindowAccess == null || !isWindowAccess)
		{
			bAdd.setDisabled(true);
			bDelete.setDisabled(true);
		}
		//

		if (reloadTreeView)
		{
			updateTreeIDOnCtx();
			getTreePanel().initTree(tree.getKey(), getWindowNo());
			Events.echoEvent(ON_DEFER_SET_SELECTED_NODE, this, null);
		}
	}

	private void updateTreeIDOnCtx()
	{
		Env.setContext(Env.getCtx(), getWindowNo(), "AD_Tree_ID", currentSelectedTreeID);
	}

	private void executeButtonProcess()
	{
		// original code → AbstractADWindowContent.executeButtonProcess0();
		int processID = MProcess.getProcess_ID(NEW_TREE_PROCESS, null);
		int recordID  = ((KeyNamePair) treeField.getSelectedItem().getValue()).getKey();
		if (processID > 0)
		{
			// scoletta@ads.it - History Records Plugin
			// vanilla 13: ProcessModalDialog aggiunge null per recordUU (7° parametro)
			ProcessModalDialog dialog = new ProcessModalDialog(this, getWindowNo(), processID,
					MTree.Table_ID, recordID, null, false);

			if (dialog.isValid())
			{
				dialog.setBorder("normal");
				getADWindowContent().getComponent().getParent().appendChild(dialog);
				getADWindowContent().showBusyMask(dialog);
				LayoutUtils.openOverlappedWindow(getADWindowContent().getComponent().getParent(), dialog,
						"middle_center");
				dialog.focus();
			}
		}
	}

	private void action_treeAdd(int RecordID)
	{
		action_treeAdd(RecordID, true);
	}

	/*
	 * fin acg: nuovo metodo con nuovo parametro per avere sicurezza sul nodo padre su cui aggiungere figli
	 */
	private void action_treeAdd(int RecordID, boolean saveOnDB)
	{
		action_treeAdd(RecordID, saveOnDB, -1);
	}

	private void action_treeAdd(int RecordID, int selectedID)
	{
		action_treeAdd(RecordID, true, selectedID);
	}

	private void action_treeAdd(int RecordID, boolean saveOnDB, int selectedID)
	{
		if (RecordID > 0)
		{
			/*
			 * fin acg: nuova gestione aggiunta su selezionato
			 */
			DefaultTreeNode<Object> selectedNode = null;
			SimpleTreeModel model = (SimpleTreeModel) (TreeModel<?>) getTreePanel().getTree().getModel();

			if (selectedID <= 0)
			{
				Treeitem selectedItem = getTreePanel().getTree().getSelectedItem();
				if (selectedItem != null)
				{
					selectedNode = selectedItem.getValue();
				}
			}
			else
			{
				selectedNode = getTreeNodeFromModel(model, selectedID);
			}

			DefaultTreeNode<Object> tn        = getTreeNodeFromModel(model, selectedID);
			DefaultTreeNode<Object> treeNode  = getTreeNodeFromModel(model, RecordID);

			if (treeNode == null)
			{
				// scoletta@ads.it - History Records Plugin
				// vanilla 13: PO.get(ctx, tableName, id, trxName) era utility Finmatica nel core 8.2.
				PO po = MTable.get(Env.getCtx(), getTableName()).getPO(RecordID, null);
				if (po == null)
				{
					return;
				}
				String  name           = (String) po.get_Value("Name");
				String  description    = (String) po.get_Value("Description");
				String  value          = (String) po.get_Value("Value");
				boolean summary        = po.get_ValueAsBoolean("IsSummary");
				String  imageIndicator = (String) po.get_Value("Action"); // Menu - Action

				name = value + " - " + name;
				//
				int parentID = -1;
				//fin acg
				if (selectedNode != null)
				{
					MTreeNode root = (MTreeNode) selectedNode.getData();
					parentID = root.getNode_ID();
				}
				else
				{
					treeNode = model.getRoot();
					MTreeNode root = (MTreeNode) treeNode.getData();
					parentID = root.getNode_ID();
				}

				DefaultTreeNode<Object> parentNode = null;
				int parentIDFromValue = -1;

				//FIN BL 11/12/2018 (tanto il parent sappiamo che è corretto)
				if (selectedNode != null && parentID > 0)
					parentIDFromValue = parentID;

				if (isTreeDrivenByValueLocal() && parentIDFromValue > 0)
				{
					parentID   = parentIDFromValue;
					parentNode = model.find(treeNode, parentID);
				}

				int seqNo = getMaxSeqnoFromParent(m_tree.getAD_Tree_ID(), parentID);

				MTreeNode newMTreeNode = new MTreeNode(RecordID, seqNo, name, description, parentID,
						summary, imageIndicator, false, null);
				DefaultTreeNode<Object> newNode = new DefaultTreeNode<Object>(newMTreeNode);

				if (tn != null)
				{
					model.addNode(tn, newNode, 0);
				}
				else if (isTreeDrivenByValueLocal() && parentNode != null)
				{
					model.addNode(parentNode, newNode, 0);
				}
				else
				{
					model.addNode(newNode);
				}

				if (saveOnDB)
					addNode(newMTreeNode, parentID);

				DefaultTreeNode<?> sNode = (DefaultTreeNode<?>) newNode;
				if (sNode != null)
				{
					int[] path = model.getPath((TreeNode<Object>) sNode);
					if (path.length > 0)
					{
						getTreePanel().getTree().renderItemByPath(path);
					}
				}
			}
		}
	} // action_treeAdd

	private void action_treeDelete(Treeitem sel)
	{
		//cannot reuse deleteNode() from ADTabPanel, it checks recordID and only does model.removeNode()
		if (sel != null)
		{
			SimpleTreeModel model = (SimpleTreeModel) (TreeModel<?>) getTreePanel().getTree().getModel();
			DefaultTreeNode<Object> treeNode = sel.getValue();
			deleteRecoursive(model, treeNode);
		}
	} // action_treeDelete

	private void deleteRecoursive(SimpleTreeModel model, DefaultTreeNode<Object> treeNode)
	{
		if (!treeNode.isLeaf())
		{
			//SN Bug #41192
			if (m_tree.isProduct() || m_tree.isBPartner() || m_tree.isMenu())
			{
				while (treeNode.getChildCount() > 0)
				{
					DefaultTreeNode<Object> node = (DefaultTreeNode<Object>) treeNode.getChildAt(0);
					deleteRecoursive(model, node);
				}
			}
			else
			{
				for (int i = 0; i < treeNode.getChildCount(); i++)
				{
					DefaultTreeNode<Object> node = (DefaultTreeNode<Object>) treeNode.getChildAt(i);
					deleteRecoursive(model, node);
				}
			}
			//SN fine
		}

		MTreeNode data = (MTreeNode) treeNode.getData();
		model.removeNode(treeNode);
		deleteNode(data);
	}

	public void deleteNode(MTreeNode item)
	{
		if (item != null)
		{
			int NodeID = item.getNode_ID();

			if (m_tree.isProduct())
			{
				MTree_NodePR node = MTree_NodePR.get(m_tree, NodeID);
				if (node != null)
					node.delete(true);
			}
			else if (m_tree.isBPartner())
			{
				MTree_NodeBP node = MTree_NodeBP.get(m_tree, NodeID);
				if (node != null)
					node.delete(true);
			}
			else if (m_tree.isMenu())
			{
				MTree_NodeMM node = MTree_NodeMM.get(m_tree, NodeID);
				if (node != null)
					node.delete(true);
			}
			else
			{
				MTree_Node node = MTree_Node.get(m_tree, NodeID);
				if (node != null)
				{
					//SN Bug #41192 TODO
					node.setIsActive(false);
					node.setSeqNo(0);
					// scoletta@ads.it - History Records Plugin
					// vanilla 13: MTree_Node.setFIN_Level(int) non esiste → set_Value
					node.set_Value("FIN_Level", 0);
					node.setParent_ID(0);
					node.save();
					//SN fine
				}
			}
		}
	} // deleteNode

	/*
	 * fin acg: addNodeWithParent
	 */
	public void addNode(MTreeNode item, int parentID)
	{
		if (item != null)
		{
			int NodeID = item.getNode_ID();

			//	May cause Error if in tree
			if (m_tree.isProduct())
			{
				MTree_NodePR node = new MTree_NodePR(m_tree, NodeID);
				if (parentID > 0)
					node.setParent_ID(parentID);
				node.setSeqNo(Integer.parseInt(item.getSeqNo()));
				node.saveEx();
			}
			else if (m_tree.isBPartner())
			{
				MTree_NodeBP node = new MTree_NodeBP(m_tree, NodeID);
				if (parentID > 0)
					node.setParent_ID(parentID);
				node.setSeqNo(Integer.parseInt(item.getSeqNo()));
				node.saveEx();
			}
			else if (m_tree.isMenu())
			{
				MTree_NodeMM node = new MTree_NodeMM(m_tree, NodeID);
				if (parentID > 0)
					node.setParent_ID(parentID);
				node.setSeqNo(Integer.parseInt(item.getSeqNo()));
				node.saveEx();
			}
			else
			{
				//SN Bug #41192
				MTree_Node node = MTree_Node.get(m_tree, NodeID);
				if (node == null)
					node = new MTree_Node(m_tree, NodeID);
				node.setIsActive(true);
				//SN fine

				if (parentID > 0)
					node.setParent_ID(parentID);
				else //SN Bug #41192
					node.set_ValueOfColumn(MTree_Node.COLUMNNAME_Parent_ID, 0);

				node.setSeqNo(Integer.parseInt(item.getSeqNo()));
				node.saveEx();
			}
		}
	} // addNode with parentID

	/**
	 * Action: Add Node to Tree
	 * @param item item
	 */
	public void addNode(MTreeNode item)
	{
		addNode(item, -1);
	} // addNode

	@Override
	public void dataStatusChanged(DataStatusEvent e)
	{
		if (m_tree != null)
		{
			if ("Deleted".equalsIgnoreCase(e.getAD_Message()))
			{
				if (e.Record_ID != null && e.Record_ID instanceof Integer
						&& ((Integer) e.Record_ID != getGridTab().getRecord_ID()))
				{
					if (m_tree.isAllNodes())
					{
						SimpleTreeModel model = (SimpleTreeModel) (TreeModel<?>) getTreePanel().getTree().getModel();
						DefaultTreeNode<Object> treeNode = getTreeNodeFromModel(model, (int) e.Record_ID);
						if (treeNode != null)
						{
							model.removeNode(treeNode);
						}
					}
				}
			}
			else if ("Saved".equals(e.getAD_Message()))
			{
				if (m_tree.isAllNodes())
				{
					action_treeAdd(getGridTab().getRecord_ID(), false);
				}
			}
		}

		//moved here from setActive() because if more than 1 tabLevel, it doesn't work
		updateTreeIDOnCtx();
		super.dataStatusChanged(e);
	}

	// scoletta@ads.it - History Records Plugin
	// getADTreeIDForRefresh() non esiste in ADTabpanel vanilla 13 → non più @Override.
	// Il metodo è mantenuto come helper privato ma non viene invocato automaticamente.
	// La logica di aggiornamento versioni per tab annidati può essere re-integrata
	// tramite dataStatusChanged in una fase successiva se necessario.
	@SuppressWarnings("unused")
	private int getADTreeIDForRefresh_NotActive()
	{
		if (getGridTab().getTabLevel() > 0)
		{
			int parentTreeID = getParentTabTreeID();
			if (defaultADTreeID != parentTreeID)
			{
				defaultADTreeID = parentTreeID;
				loadVersions(true, defaultADTreeID);
			}
		}
		// chiamata originale: return super.getADTreeIDForRefresh(); -- non più disponibile
		return defaultADTreeID;
	}

	private int getParentTabTreeID()
	{
		return Env.getContextAsInt(Env.getCtx(), getWindowNo(),
				getGridTab().getParentTab().getTabNo(), "AD_Tree_ID");
	}

	//FIN BL 12/12/2018 metodo per ottenere la sequenza massima a partire da un parent
	private int getMaxSeqnoFromParent(int p_treeID, int p_parentID)
	{
		int maxSeqno = 0;
		String query = "select max(seqno)"
				     + "  from ad_treenode"
				     + " where ad_tree_id = " + p_treeID;

		if (p_parentID > 0)
		{
			query += " and parent_id = " + p_parentID;
		}
		else if (p_parentID == 0)
		{
			query += " and parent_id = " + p_parentID;
		}
		else
		{
			query += " and parent_id is null";
		}

		maxSeqno = DB.getSQLValue(null, query);
		maxSeqno++;

		return maxSeqno;
	}

	/**
	 * scoletta@ads.it - History Records Plugin
	 * getTreeNodeFromModel() non esiste in ADTabpanel vanilla 13.
	 * Reimplementato tramite SimpleTreeModel.find() che esiste in vanilla 13.
	 */
	private DefaultTreeNode<Object> getTreeNodeFromModel(SimpleTreeModel model, int recordId)
	{
		return model.find(null, recordId);
	}

	/**
	 * scoletta@ads.it - History Records Plugin
	 * navigateTo(DefaultTreeNode<MTreeNode>) è private in ADTabpanel vanilla 13.
	 * navigateToNode() è la versione locale con stessa logica (adattata per Object generic).
	 */
	private void navigateToNode(DefaultTreeNode<Object> value)
	{
		if (value == null)
			return;
		Object data = value.getData();
		if (!(data instanceof MTreeNode))
			return;
		MTreeNode treeNode = (MTreeNode) data;
		int nodeID = treeNode.getNode_ID();

		int size = getGridTab().getRowCount();
		int row = -1;
		for (int i = 0; i < size; i++)
		{
			if (getGridTab().getKeyID(i) == nodeID)
			{
				row = i;
				break;
			}
		}
		if (row == -1)
		{
			if (getGridTab().getCurrentRow() >= 0)
				getGridTab().setCurrentRow(getGridTab().getCurrentRow(), true);
			return;
		}
		getADWindowContent().onTreeNavigate(getGridTab(), row);
	}

	/**
	 * scoletta@ads.it - History Records Plugin
	 * isTreeDrivenByValue() è private in ADTabpanel vanilla 13.
	 * Reimplementato localmente tramite SimpleTreeModel.isTreeDrivenByValue().
	 */
	private boolean isTreeDrivenByValueLocal()
	{
		if (getTreePanel() == null
				|| getTreePanel().getTree() == null
				|| getTreePanel().getTree().getModel() == null)
			return false;
		SimpleTreeModel model = (SimpleTreeModel) (TreeModel<?>) getTreePanel().getTree().getModel();
		return model.isTreeDrivenByValue();
	}

}
