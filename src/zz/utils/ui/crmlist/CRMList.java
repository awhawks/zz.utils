/*
 * Created on Feb 24, 2004
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package zz.utils.ui.crmlist;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListCellRenderer;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import zz.utils.ItemAction;
import zz.utils.ui.GridStackLayout;
import zz.utils.ui.UIUtils;

/**
 * Displays a list with a set of buttons that allow to create, remove or move elements.
 * CRM stands for Create, Remove, Move.
 * @see zz.utils.ui.crmlist.CRMListModel
 * @author gpothier
 */
public class CRMList extends JPanel implements ActionListener, ListDataListener
{
	private static final String MOVE_DOWN = "moveElementDown";
	private static final String MOVE_UP = "moveElementUp";
	private static final String REMOVE = "removeElement";
	private static final String CREATE = "createElement";
	private JList itsList;
	
	private Action itsCreateAction;
	private Action itsRemoveAction;
	private Action itsMoveDownAction;
	private Action itsMoveUpAction;
	private JButton itsCreateButton;
	private JButton itsRemoveButton;
	private JButton itsMoveDownButton;
	private JButton itsMoveUpButton;
	private CRMListModel itsModel;

	public CRMList ()
	{
		this (new AbstractJavaCRMListModel()
				{
					@Override
					protected Object newElement()
					{
						return null;
					}
				});
	}
	
	public CRMList (CRMListModel aModel) 
	{
		itsList = new JList (aModel);
		createUI();
		setModel(aModel);
	}
	
	public void setModel (CRMListModel aModel)
	{
		if (itsModel != null) itsModel.removeListDataListener(this);
		
		itsModel = aModel;
		
		itsList.setModel(itsModel);
		if (itsModel != null) itsModel.addListDataListener(this);
		
		updateButtonsState();
	}
	
	private CRMListModel getEditableListModel ()
	{
		return (CRMListModel) itsList.getModel();
	}
	
	private void createUI ()
	{
		setPreferredSize(new Dimension (200, 300));

		itsCreateAction = new MyAction ("+", CREATE);
		itsRemoveAction = new MyAction ("-", REMOVE);
		itsMoveUpAction = new MyAction ("up", MOVE_UP);
		itsMoveDownAction = new MyAction ("dn", MOVE_DOWN);
		
		setLayout(new BorderLayout ());
		JPanel theButtonsPanel = new JPanel (new GridStackLayout (1));
		
		add (new JScrollPane (itsList), BorderLayout.CENTER);
		JPanel theButtonsBuffer = new JPanel (new BorderLayout ());
		theButtonsBuffer.add (theButtonsPanel, BorderLayout.NORTH);
		add (theButtonsBuffer, BorderLayout.EAST);
		
		itsCreateButton = new JButton (itsCreateAction);
		itsRemoveButton = new JButton (itsRemoveAction);
		itsMoveDownButton = new JButton (itsMoveDownAction);
		itsMoveUpButton = new JButton (itsMoveUpAction);
		itsCreateButton.setMargin(UIUtils.NULL_INSETS);
		itsRemoveButton.setMargin(UIUtils.NULL_INSETS);
		itsMoveDownButton.setMargin(UIUtils.NULL_INSETS);
		itsMoveUpButton.setMargin(UIUtils.NULL_INSETS);

		theButtonsPanel.add (itsCreateButton);
		theButtonsPanel.add (itsRemoveButton);
		theButtonsPanel.add (itsMoveUpButton);
		theButtonsPanel.add (itsMoveDownButton);
		
		getSelectionModel ().addListSelectionListener(new ListSelectionListener ()
				{
			public void valueChanged(ListSelectionEvent aE)
			{
				selectionChanged();
			}
			
		});
	}
	
	public ListSelectionModel getSelectionModel()
	{
		return itsList.getSelectionModel();
	}
	
	public Object getSelectedValue ()
	{
		return itsList.getSelectedValue();
	}

	private void selectionChanged ()
	{
		updateButtonsState();
	}
	
	public JButton getCreateButton()
	{
		return itsCreateButton;
	}

	public JButton getMoveDownButton()
	{
		return itsMoveDownButton;
	}

	public JButton getMoveUpButton()
	{
		return itsMoveUpButton;
	}

	public JButton getRemoveButton()
	{
		return itsRemoveButton;
	}

	private void updateButtonsState ()
	{
		int theSelectedIndex = itsList.getSelectedIndex();

		boolean theCanCreate = getEditableListModel().canCreateElement();
		boolean theCanRemove = getEditableListModel().canRemoveElement(theSelectedIndex);
		boolean theCanMoveUp = getEditableListModel().canMoveElement(theSelectedIndex, theSelectedIndex-1);
		boolean theCanMoveDown = getEditableListModel().canMoveElement(theSelectedIndex, theSelectedIndex+1);
		
		itsCreateAction.setEnabled(theCanCreate);
		itsRemoveAction.setEnabled(theCanRemove);
		itsMoveUpAction.setEnabled(theCanMoveUp);
		itsMoveDownAction.setEnabled(theCanMoveDown);
	}
	
	public void actionPerformed (ActionEvent aEvent)
	{
		String theCommandString = aEvent.getActionCommand();
		int theSelectedIndex = itsList.getSelectedIndex();
		
		if (CREATE.equals(theCommandString)) 
		{
			theSelectedIndex = getEditableListModel().createElement();
		}
		else if (REMOVE.equals(theCommandString)) 
		{
			getEditableListModel().removeElement(theSelectedIndex);
			theSelectedIndex = Math.min (theSelectedIndex, getEditableListModel().getSize()-1); 
		}
		else if (MOVE_UP.equals(theCommandString)) 
		{	
			getEditableListModel().moveElement(theSelectedIndex, theSelectedIndex-1);
			theSelectedIndex--;
		}
		else if (MOVE_DOWN.equals(theCommandString))
		{
			getEditableListModel().moveElement(theSelectedIndex, theSelectedIndex+1);
			theSelectedIndex++;
		}
		else assert false;
		
		if (theSelectedIndex >= 0) itsList.setSelectedIndex(theSelectedIndex);
		
		updateButtonsState();
	}
	
	public void contentsChanged(ListDataEvent aE)
	{
		updateButtonsState();
	}

	public void intervalAdded(ListDataEvent aE)
	{
		updateButtonsState();
	}

	public void intervalRemoved(ListDataEvent aE)
	{
		updateButtonsState();
	}

	public void setCellRenderer (ListCellRenderer aRenderer)
	{
		itsList.setCellRenderer(aRenderer);
	}
	
	private class MyAction extends ItemAction
	{
		public MyAction (String aTitle, String aCommandString)
		{
			setActionListener(CRMList.this);
			setTitle (aTitle);
			setCommandString(aCommandString);
		}
	}

}
