package arr.ui;


import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

import org.eclipse.core.resources.IProject;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Table;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ViewPart;

import arr.apriori.AprioriOutput;
import arr.general.ARRJavaPackage;
import arr.utils.FileUtilities;
import arr.utils.ProjectUtilities;


public class ARRDataView extends ViewPart {

	/**
	 * The ID of the view as specified by the extension.
	 */
	public static final String ID = "architecture_rules_recovery.views.ARRDataView";

	private TableViewer viewer;
	
	private Action actionRunProject;
	private Action actionRunWorkspace;
	private Action actionExportDependencies;
	private Action actionExportSPMF;
	private Action actionOpenProperties;
	 

	/**
	 * The constructor.
	 */
	public ARRDataView() {
	}

	/**
	 * This is a callback that will allow us
	 * to create the viewer and initialize it.
	 */
	public void createPartControl(Composite parent) {
		// Instantiates the tableViewer with the properties:
		// SCROLL H AND V, FULL SELECTION (can only select a whole row, with all columns)
		viewer = new TableViewer(parent, SWT.H_SCROLL | SWT.V_SCROLL | SWT.FULL_SELECTION );
		createColumns(parent);
		final Table table = viewer.getTable();
		// Creates the form table
	    final FormData fd_table = new FormData();
		fd_table.bottom = new FormAttachment(100, -1);
		fd_table.top = new FormAttachment(0, -5);
		fd_table.right = new FormAttachment(100, -1);
		fd_table.left = new FormAttachment(0, 1);
		table.setLayoutData(fd_table);
		table.setLinesVisible(true);
		table.setHeaderVisible(true);
	    table.setLayoutData(new GridData(GridData.FILL_BOTH));

		// Sets the viewContentProvider that will take care of contents of the table
	    viewer.setContentProvider(new ArrayContentProvider());
	    viewer.setInput(ProjectUtilities.getaOuts());


		
		PlatformUI.getWorkbench().getHelpSystem().setHelp(viewer.getControl(), "Architecture_Rules_Recovery.viewer");
		makeActions();
		hookContextMenu();
		contributeToActionBars();	
	}
	
	private void createColumns(Composite parent)
	{

		TableViewerColumn targetPackageColumn = new TableViewerColumn (viewer, SWT.NONE);
		targetPackageColumn.getColumn().setWidth(300);
		targetPackageColumn.getColumn().setText("Target Package");
		targetPackageColumn.setLabelProvider(new ColumnLabelProvider() {
			  @Override
			  public String getText(Object element) {
			    AprioriOutput p = (AprioriOutput) element;
			    return p.getTargetPackage().getName();
			  }
			});
		TableViewerColumn usedPackagesColumn = new TableViewerColumn (viewer, SWT.NONE);
		usedPackagesColumn.getColumn().setWidth(300);
		usedPackagesColumn.getColumn().setText("Used Packages");
		usedPackagesColumn.setLabelProvider(new ColumnLabelProvider() {
			  @Override
			  public String getText(Object element) {
			    AprioriOutput e = (AprioriOutput) element;
			    String output = "";
			    for(ARRJavaPackage a : e.getjPackages())
			    {
			    	output = output + a.getName() + ", " ;
			    }
			    return output;
			  }
			});
		
		TableViewerColumn suportColumn = new TableViewerColumn (viewer, SWT.NONE);
		suportColumn.getColumn().setWidth(100);
		suportColumn.getColumn().setText("Support");
		suportColumn.setLabelProvider(new ColumnLabelProvider() {
			  @Override
			  public String getText(Object element) {
			    AprioriOutput e = (AprioriOutput) element;
			    return String.valueOf(e.getSuport());
			  }
			});
	}
	
	public void refresh() 
	{
		viewer.refresh();
	}
	
	private void hookContextMenu() {
		MenuManager menuMgr = new MenuManager("#PopupMenu");
		menuMgr.setRemoveAllWhenShown(true);
		menuMgr.addMenuListener(new IMenuListener() {
			public void menuAboutToShow(IMenuManager manager) {
				ARRDataView.this.fillContextMenu(manager);
			}
		});
		Menu menu = menuMgr.createContextMenu(viewer.getControl());
		viewer.getControl().setMenu(menu);
		getSite().registerContextMenu(menuMgr, viewer);
	}

	private void contributeToActionBars() {
		IActionBars bars = getViewSite().getActionBars();
		fillLocalPullDown(bars.getMenuManager());
		fillLocalToolBar(bars.getToolBarManager());
	}

	private void fillLocalPullDown(IMenuManager manager) {
		manager.add(actionRunProject);
		//manager.add(new Separator());
		manager.add(actionRunWorkspace);
		manager.add(new Separator());
		manager.add(actionExportDependencies);
		manager.add(actionExportSPMF);
		manager.add(new Separator());
		manager.add(actionOpenProperties);
	}

	private void fillContextMenu(IMenuManager manager) {
		manager.add(actionRunProject);
		manager.add(actionRunWorkspace);
		manager.add(new Separator());
		manager.add(actionExportDependencies);
		manager.add(actionExportSPMF);
		manager.add(new Separator());
		manager.add(actionOpenProperties);
		// Other plug-ins can contribute there actions here
		manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
	}
	
	private void fillLocalToolBar(IToolBarManager manager) {
		manager.add(actionRunProject);
		manager.add(actionRunWorkspace);
		manager.add(new Separator());
		manager.add(actionExportDependencies);
		manager.add(actionExportSPMF);
		manager.add(new Separator());
		manager.add(actionOpenProperties);
	}

	@SuppressWarnings("deprecation")
	private void makeActions() {
		actionRunProject = new Action() {
			public void run() {
				
				ArrayList<IProject> projects = new ArrayList<IProject>();
				projects.add(ProjectUtilities.getCurrentProject());
				
				if(projects.size() == 0)
				{
					MessageSystem.runProjectFirst();
					return;
				}
				
				if(ARRRun.run(projects))
					MessageSystem.sucessfullyFinished();
				
				return;
			}
		};
		actionRunProject.setText("Project Architecture Recovery");
		actionRunProject.setToolTipText("Run ARR plugin on the selected project in the Eclipse Package Explorer view. (This doesn't fetch the other possible projects in the workspace)");
		actionRunProject.setImageDescriptor(PlatformUI.getWorkbench().getSharedImages().
			getImageDescriptor(ISharedImages.IMG_OBJ_PROJECT_CLOSED));
		
		actionRunWorkspace = new Action() {
			public void run() {
				ArrayList<IProject> projects = new ArrayList<IProject>();
		        projects.addAll(Arrays.asList(ProjectUtilities.getProjectsFromWorkspace()));
		        
		        // Shows a message that tells that the code already finished
		        if(ARRRun.run(projects))
				MessageSystem.sucessfullyFinished();
				
		        return;
			}
		};
		actionRunWorkspace.setText("Workspace Architecture Recovery");
		actionRunWorkspace.setToolTipText("Runs Architecture Rules Recovery plugin using as input all projects in the current workspace.");
		actionRunWorkspace.setImageDescriptor(PlatformUI.getWorkbench().getSharedImages().
				getImageDescriptor(ISharedImages.IMG_OBJ_PROJECT));
		
		actionExportDependencies = new Action() {
			public void run()
			{
				// If ARR was not run yet
				if(!ProjectUtilities.getDependencyMatrixStatus())
				{
					MessageSystem.runPluginFirst();
					return;
				}
			    FileDialog dialog = new FileDialog(Display.getCurrent().getActiveShell(), SWT.SAVE);
			    dialog.setOverwrite(true);
			    dialog.setFilterExtensions(new String[] { "*.csv" });
			    String csvFile = dialog.open();
			    if (csvFile != null) {
			    	File selectedFile = new File(csvFile);
				    try {
						FileUtilities.createCSVFileForDependencies(ProjectUtilities.getDependencyMatrix(), selectedFile);
					} catch (IOException e) {
						MessageSystem.fileProblem();
						e.printStackTrace();
					}
			    }
			    
			}
		};
		actionExportDependencies.setText("Export Dependencies");
		actionExportDependencies.setToolTipText("Export all dependencies that were used as input to the machine learning algorithm to a CSV file.");
		actionExportDependencies.setImageDescriptor(PlatformUI.getWorkbench().getSharedImages().
				getImageDescriptor(ISharedImages.IMG_ETOOL_SAVE_EDIT));
		
		actionExportSPMF = new Action() {
			public void run()
			{
				// If ARR was not run yet
				if(!ProjectUtilities.getDependencyMatrixStatus())
				{
					MessageSystem.runProjectFirst();
					return;
				}
			    FileDialog dialog = new FileDialog(Display.getCurrent().getActiveShell(), SWT.SAVE);
			    dialog.setOverwrite(true);
			    dialog.setFilterExtensions(new String[] { "*.csv" });
			    String csvFile = dialog.open();
			    if (csvFile != null) {
			    	File selectedFile = new File(csvFile);
				    try {
						FileUtilities.createCSVFileForApriori(ProjectUtilities.getaOuts(), selectedFile);
					} catch (IOException e) {
						MessageSystem.fileProblem();
						e.printStackTrace();
					}
			    }
			    
			}
		};
		actionExportSPMF.setText("Export Calculated Values");
		actionExportSPMF.setToolTipText("Export all found itemsets by the algorithm to a CSV file.");
		actionExportSPMF.setImageDescriptor(PlatformUI.getWorkbench().getSharedImages().
				getImageDescriptor(ISharedImages.IMG_ETOOL_SAVEALL_EDIT));
		
	
		actionOpenProperties = new Action() {
			public void run()
			{
				OptionsView oView = new OptionsView(Display.getCurrent());
				oView.open();
			}
		};
		actionOpenProperties.setText("Open Algorithm Options");
		actionOpenProperties.setToolTipText("Open algorithm selection options and it's parameters.");
		actionOpenProperties.setImageDescriptor(PlatformUI.getWorkbench().getSharedImages().
				getImageDescriptor(ISharedImages.IMG_OBJS_TASK_TSK));

	}
	
	public TableViewer getViewer()
	{
		return viewer;
	}

	/**
	 * Passing the focus request to the viewer's control.
	 */
	public void setFocus() {
		viewer.getControl().setFocus();
	}
}