package arr.ui;

import java.util.ArrayList;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.transaction.RecordingCommand;
import org.eclipse.emf.transaction.TransactionalEditingDomain;
import org.eclipse.graphiti.dt.IDiagramTypeProvider;
import org.eclipse.graphiti.features.IAddFeature;
import org.eclipse.graphiti.features.IFeatureProvider;
import org.eclipse.graphiti.features.context.impl.AddConnectionContext;
import org.eclipse.graphiti.features.context.impl.AddContext;
import org.eclipse.graphiti.mm.pictograms.Diagram;
import org.eclipse.graphiti.mm.pictograms.PictogramElement;
import org.eclipse.graphiti.mm.pictograms.Shape;
import org.eclipse.graphiti.services.Graphiti;
import org.eclipse.graphiti.ui.services.GraphitiUi;

import arr.apriori.AprioriOutput;
import arr.general.ARRJavaPackage;
import arr.general.ARRJavaPackageInterface;
import arr.general.ArchitecturalDependency;
//import arr.general.ArchitecturalDependency;
import arr.utils.ProjectUtilities;

public class CreateDiagramCommand extends RecordingCommand {

	private TransactionalEditingDomain editingDomain;
	private String diagramName;
	private Resource createdResource;
	public static ArrayList<ARRJavaPackageInterface> allPackages;
	
	public CreateDiagramCommand(TransactionalEditingDomain editingDomain, String diagramName) {
		super(editingDomain);
		this.editingDomain = editingDomain;
		this.diagramName = diagramName;
	}

	@Override
	protected void doExecute() {

		ArrayList<AprioriOutput> aOuts = ProjectUtilities.getaOuts();
		ArrayList<ArchitecturalDependency> archDeps = new ArrayList<ArchitecturalDependency>();
		
		// Get all JavaPackages that will be used
		allPackages = new ArrayList<ARRJavaPackageInterface>();
		
		for(AprioriOutput aOut : aOuts)
		{
			for(ARRJavaPackage a : aOut.getjPackages())
			{
				if(!allPackages.contains(a))
				{
					allPackages.add(a);
				}
			}
			if(!allPackages.contains(aOut.getTargetPackage()))
			{
				allPackages.add(aOut.getTargetPackage());
			}
		}

		int colorID = 0;
		//Para todas as sa�das do apriori
		for(AprioriOutput aOut : aOuts)
		{
			boolean alreadyHaveSource = false;
			ArchitecturalDependency ref = null;
			//Para todas as archdeps que eu j� salvei
			for(ArchitecturalDependency archDep : archDeps)
			{
				//Se eu tenho alguma source que a archdep � esse target package do apriori
				if(archDep.getTarget() == aOut.getTargetPackage())
				{
					alreadyHaveSource = true;
					ref = archDep;
				}
			}
			//para todos os jpackages dessa regra gerada, se ela existe dentro do meu conjunto de archdeps
			for(ARRJavaPackage innerPackage : aOut.getjPackages())
			{
				//se a regra j� existe e est� l�, atualiza o valor de suporte (TODO: FAZER V�RIAS REGRAS IGUAIS?)
				if(alreadyHaveSource)
				{
					if(ref.getSupport() <= aOut.getSuport())
						ref.setSupport(aOut.getSuport());
				}
				//caso contr�rio, cria regra nas archdeps
				else
				{
					ArchitecturalDependency ad = new ArchitecturalDependency(aOut.getTargetPackage(), innerPackage,aOut.getSuport(), colorID);
					colorID++;
					archDeps.add(ad);
				}
				alreadyHaveSource = false;	
			}
			
		}
		
		//Agora que tenho todas as depend�ncias preciso s� criar eles no diagrama.
		

		// Create the diagram and its file
		Diagram diagram = Graphiti.getPeCreateService().createDiagram("arrdiagram", diagramName, true); //$NON-NLS-1$		
		IFolder diagramFolder = ProjectUtilities.getCurrentProject().getFolder("/diagrams/"); //$NON-NLS-1$
		IFile diagramFile = diagramFolder.getFile(diagramName + ".diagram"); //$NON-NLS-1$
		URI uri = URI.createPlatformResourceURI(diagramFile.getFullPath().toString(), true);
		createdResource = editingDomain.getResourceSet().createResource(uri);
		createdResource.getContents().add(diagram);

		IDiagramTypeProvider dtp = GraphitiUi.getExtensionManager().createDiagramTypeProvider(diagram,
				"arr.graphiti.diagram.DiagramTypeProvider"); //$NON-NLS-1$
		IFeatureProvider featureProvider = dtp.getFeatureProvider();
		
		ArrayList<PictogramElement> peList = new ArrayList<PictogramElement>();
		
		// Add all packages to diagram
		int x = 25;
		int y = 25;
		for (int i = 0; i < allPackages.size(); i++) {
			// Create the context information
			AddContext addContext = new AddContext();
			addContext.setNewObject(allPackages.get(i));
			addContext.setTargetContainer(diagram);
			addContext.setX(x);
			addContext.setY(y);
			addContext.setHeight(100);
			addContext.setWidth(300);
			x = x + 25;
			y = y + 25;
			IAddFeature addFeature = featureProvider.getAddFeature(addContext);
			if (addFeature.canAdd(addContext)) {
				peList.add(addFeature.add(addContext));
			}
		}
		
		//add all references between them

		for(ArchitecturalDependency archDep : archDeps)
		{
			
			for(PictogramElement p1 : peList)
			{
				ARRJavaPackage srcPackage = (ARRJavaPackage) p1.getLink().getBusinessObjects().get(0);
				//System.out.println("SOURCE: " + srcPackage.getName() + " com " + archDep.getSource().getName());
				if(srcPackage.equals(archDep.getSource()))
				{
					for(PictogramElement p2 : peList)
					{
						ARRJavaPackage tgtPackage = (ARRJavaPackage) p2.getLink().getBusinessObjects().get(0);
						//System.out.println("TARGET: " + tgtPackage.getName() + " com " + archDep.getTarget().getName());
						if(tgtPackage.equals(archDep.getTarget()))
						{
							//System.out.print(archDep.getSource().getName() + " " + archDep.getTarget().getName() + " " + String.valueOf(archDep.getSupport()) + "\n");
							Shape shape1 = (Shape) p1;
							Shape shape2 = (Shape) p2;
							// Create the context information
							AddConnectionContext addConContext = new AddConnectionContext(shape1.getAnchors().get(0), shape2.getAnchors().get(0));
							addConContext.setNewObject(archDep);
							
							addConContext.setTargetContainer(diagram);

							IAddFeature addFeature = featureProvider.getAddFeature(addConContext);
							if (addFeature.canAdd(addConContext)) {
								addFeature.add(addConContext);
							}
						}
					}
				}
			}		
		}

	}

	/**
	 * @return the createdResource
	 */
	public Resource getCreatedResource() {
		return createdResource;
	}
}
