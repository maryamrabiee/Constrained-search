package phylonet.coalescent;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

import com.sun.org.apache.bcel.internal.generic.NEW;

import phylonet.lca.SchieberVishkinLCA;
import phylonet.tree.io.NewickReader;
import phylonet.tree.io.ParseException;
import phylonet.tree.model.TMutableNode;
import phylonet.tree.model.TNode;
import phylonet.tree.model.Tree;
import phylonet.tree.model.sti.STINode;
import phylonet.tree.model.sti.STITree;
import phylonet.tree.util.Trees;

class NodeInfo{
	private String color="";	
	private int greenDescendents = 0;
	public NodeInfo(String color, int greenDes){
		this.color = color;
		this.greenDescendents = greenDes;
	}
	public NodeInfo(int greenDes){
		this.greenDescendents = greenDes;
	}
	
	public String getColor() {
		return color;
	}
	public int getGrDes() {
		return greenDescendents;
	}
	
}
public class TreeCompletion {
	
	public static void main(String[] args) throws IOException, ParseException{
		//String tr1 = "((1,(2,3)a)b,(4,(5,(6,7)d)c));";
//		String tr1 = "((1,2)a,(3,(4,(6,(5,7))d)c));";
		String tr1 = "((7,2)a,(3,(4,(6,(5,9,18))d)c)f,(11,12,(22,1),30)g,99)r;";
		String tr2 = "((3,99),((11,12),(5,9)a)b)c;";
		//(18,(7,2),((3,99),4),((11,(5,9)a,0,22,30)b,6))c;
//		String tr2 = "(9,7,3,4);";
		NewickReader nr = new NewickReader(new StringReader(tr1));
		STITree<Double> gt = new STITree<Double>(true);
		nr.readTree(gt);
		
		NewickReader nr2 = new NewickReader(new StringReader(tr2));
		STITree<Double> st = new STITree<Double>(true);
		nr2.readTree(st);
		STINode newNode = new STITree(((STINode<Double>) gt.getNode("18")).toNewick()).getRoot();
//		TNode nn =st.getRoot().createChild(gt.getNode("18"));
//		((TMutableNode) nn).adoptChild(newNode);
//		adoptChild(newNode);

//		TNode n = st.getNode("18");
//		if(n==null){
//			System.err.println("heyy");
//		}
		
//		for (TNode gtNode : gt.postTraverse()) {
//			if(gtNode.getName().equals("c")){
//				System.err.println(gtNode.getID()+"**");
//				System.err.println(((STINode<Double>) gtNode).toNewick());
//			TMutableNode newNode = new NewickReader(new StringReader(((STINode<Double>) gtNode).toNewick())).readTree().getRoot();
//			st = addToTree(st,(STINode) st.getNode(0),(STINode)gtNode);
//
//			}
//		}

		System.err.println(st.toNewick());
		st = treeCompletion(gt,st);
		System.err.println("out: "+st.toNewick());

	}
	
	static STITree addToTree(STITree tree , STINode adoptingNode, STINode toMoveNode){
		
		STINode newNode = null;
		try {
			newNode = new STITree(((STINode<Double>) toMoveNode).toNewick()).getRoot();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if(!adoptingNode.isRoot()){
			STINode newinternalnode = adoptingNode.getParent().createChild();
			newinternalnode.adoptChild(adoptingNode);
			newinternalnode.createChild(newNode);
//			if(null != tree.getNode(newNode.getName()))
//				System.err.println("heyyy");
			return tree;
		}
		else{

			STINode newinternalnode = adoptingNode.createChild();
			TNode child = (TNode) adoptingNode.getChildren().iterator().next();
			newinternalnode.adoptChild((TMutableNode) child );
			newinternalnode.createChild(newNode);
			return tree;
			
		}
		
	}

	
static STITree addToTreePolytomy2(STITree tree , STINode adoptingNode, ArrayList<STINode> redChildren){
		
		ArrayList<STINode> newnodes = new ArrayList<STINode>();
		try {
			for(STINode n : redChildren){
				if(!n.isLeaf())
					n.setName("");
				newnodes.add(new STITree(((STINode<Double>)n).toNewick()).getRoot());
		}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if(adoptingNode.isLeaf()){			
			for(STINode n : newnodes){
				adoptingNode.getParent().createChild(n);
			}
		}
		else{
			for(STINode n : newnodes){
				adoptingNode.createChild(n);
			}			
		}
		return tree;
	}

static STITree addToTreePolytomy(STITree tree , STINode adoptingNode, ArrayList<STINode> redChildren){
		
		ArrayList<STINode> newnodes = new ArrayList<STINode>();
		try {
			for(STINode n : redChildren){
				if(!n.isLeaf())
					n.setName("");
					newnodes.add(new STITree(((STINode<Double>)n).toNewick()).getRoot());
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		if(!adoptingNode.isRoot()){
			
			STINode newinternalnode = adoptingNode.getParent().createChild();
			newinternalnode.adoptChild(adoptingNode);
			for(STINode n : newnodes){
				newinternalnode.createChild(n);
			}
			
			return tree;
		}
		else{
				
			STINode newinternalnode = adoptingNode.createChild();
			TNode child = (TNode) adoptingNode.getChildren().iterator().next();
			newinternalnode.adoptChild((TMutableNode) child );
			for(STINode n : newnodes)
				newinternalnode.createChild(n);
			return tree;
			

		}
		
	}
	
	static void nodeColoring(Tree stTree, Tree gtTree) {
		
		
		for (TNode node : stTree.postTraverse()) {
			if(node.isLeaf())
				((STINode) node).setData(new NodeInfo(1));
			else{
				int greenCount = 0;
				for (TNode child:node.getChildren())		
					greenCount += ((NodeInfo) ((STINode) child).getData()).getGrDes();
				((STINode) node).setData(new NodeInfo(greenCount));	
		
			}
		}
			
		Set<String> stLeaves = new HashSet<String>(Arrays.asList(stTree.getLeaves())); 
		
		for (TNode gtNode : gtTree.postTraverse()) {
			if(gtNode.isLeaf()){
				String name = gtNode.getName();
				if (stLeaves.contains(name)){
					((STINode) gtNode).setData(new NodeInfo("G", 1));
				}
				else{
					((STINode) gtNode).setData(new NodeInfo("R", 0));
				}
			}
			else{
				boolean allred = true;
				boolean allgreen = true;
				boolean hasred = false;
				int greenCount = 0;
				for (TNode child: gtNode.getChildren()){
					
					NodeInfo info = (NodeInfo) ((STINode) child).getData();
					String data = info.getColor();
					greenCount += info.getGrDes();
//					String data = (String) ((STINode) child).getData();
					if(data.equals("B") || data.equals("BM") ){
						allred = false;
						allgreen = false;
					}
					else if(data.equals("G")){
						allred = false;
					}
					else if(data.equals("R")){
						allgreen = false;
						hasred = true;
					}
				}
					if(allred) 
						((STINode) gtNode).setData(new NodeInfo("R", greenCount));
					else if(allgreen)
						((STINode) gtNode).setData(new NodeInfo("G", greenCount));
					else if(hasred)
						((STINode) gtNode).setData(new NodeInfo("BM", greenCount));
					else
						((STINode) gtNode).setData(new NodeInfo("B", greenCount));
			}
		}
		
	}
	
	static ArrayList<STITree> treeCompletionRepeat(STITree gTree, STITree sTree, int REPEATS){
		SchieberVishkinLCA lcaLookup = new SchieberVishkinLCA(sTree);
		String[] gtLeaves = gTree.getLeaves();
		String[] stLeaves = sTree.getLeaves();
		List<String> common = new ArrayList<String>(Arrays.asList(gtLeaves));
		common.retainAll(Arrays.asList(stLeaves));
		ArrayList<STITree> results = new ArrayList<STITree>();
		ArrayList<STITree> temps = new ArrayList<STITree>();
		for(int i=0; i< REPEATS; i++){
			temps.add(new STITree(sTree));
		}
		
		ArrayList<Integer> randomRoots = new ArrayList<Integer>();
		for(int i=0;i< REPEATS && i < common.size() ;i++){
			randomRoots.add(GlobalMaps.random.nextInt(common.size()));
		}

		for(int i=0 ; i< REPEATS && i < common.size(); i++){
			String root = common.get(randomRoots.get(i));
			System.err.println("Both species tree and gene tree repeat "+i+" are rooted at "+root);
			temps.get(i).rerootTreeAtNode(temps.get(i).getNode(root));
			gTree.rerootTreeAtNode(gTree.getNode(root));
			results.add(treeCompletion(gTree, temps.get(i)));
		}
		return results;
		
		
	}
	static STITree treeCompletion(STITree gTree, STITree sTree){
		nodeColoring(sTree, gTree);
		HashMap<Integer, Integer> LCAMap = createLCAMap(sTree, gTree);
	  
	        // Create an empty stack and push root to it 
	        Stack<TNode> nodeStack = new Stack<TNode>(); 
	        nodeStack.push(gTree.getRoot());
	        while (nodeStack.empty() == false) { 
	              
	            // Pop the top item from stack and print it 
	            TNode mynode = nodeStack.peek();  
	            NodeInfo info = (NodeInfo) ((STINode) mynode).getData();
				String data = info.getColor();

	            if(data.equals("BM")){
	            	
	            	int childrenCount = mynode.getChildCount();
	            	int redchild = 0;
	            	ArrayList<STINode> redChildren = new ArrayList<STINode>();
	            	ArrayList<STINode> nonRed = new ArrayList<STINode>();
	            	for(TNode child:mynode.getChildren()){
						String childData = (String) ((NodeInfo) ((STINode) child).getData()).getColor();
	            		if(childData.equals("R")){
	            			redChildren.add((STINode) child);
	            			
	            			redchild += 1;
	            		}
	            		else{
	            			nonRed.add((STINode) child);
	            		}
	            	}
	            	int id = LCAMap.get(mynode.getID());
	    	        STINode snode = sTree.getNode(id);
	    	        
	    	        if(nonRed.size()==1 && ((NodeInfo) snode.getData()).getGrDes() == ((NodeInfo) nonRed.get(0).getData()).getGrDes()){
	    	        		sTree = addToTreePolytomy(sTree, snode,  redChildren);
	    	        }
	    	        else{
	    	        		sTree = addToTreePolytomy2(sTree, snode,  redChildren);
	    	        }
	    	        
	    	        //if there is only one non-red child,it means that we definitely don't have this 
	    	        //node in the other tree and we should create a new node
////	    	        if(redchild >= 1 && mynode.getChildCount()-redchild == 1){	
////	    	        	sTree = addToTreePolytomy(sTree, snode,  redChildren);
////	    	        }
//	    	        if(redchild ==1 && mynode.getChildCount() == 2){	            	
//		    	        sTree = addToTree(sTree, snode, (STINode) redChildren.get(0));		    			
//	            	}
//	            	else{
//	            		sTree = addToTreePolytomy(sTree, snode,  redChildren);
//	            	}
	    	        
	    	        //----------------------------
				}
	            nodeStack.pop(); 
	            
	            // Push children of current node to the stack
	            //since at first we added children of the root, we skip it here

	            for(TNode t: mynode.getChildren())
	            		nodeStack.push(t); 

	    }  
	        return sTree;
	}
	
	static HashMap<Integer,Integer> createLCAMap(Tree stTree, Tree gtTree) {

		HashMap<Integer, Integer> LCAMap = new HashMap<Integer, Integer>();
		SchieberVishkinLCA lcaLookup = new SchieberVishkinLCA(stTree);

		Stack<TNode> stack = new Stack<TNode>();
		for (TNode gtNode : gtTree.postTraverse()) {
					if (gtNode.isLeaf()) {

						String color = (String) ((NodeInfo) ((STINode) gtNode).getData()).getColor();
						
						if(color.equals("G")){
							TNode t = stTree.getNode(gtNode.getName());
							stack.push(t);
//							LCAMap.put(gtNode.getID(), new ArrayList<Integer>(Arrays.asList(t.getID(),0)));
							LCAMap.put(gtNode.getID(), t.getID());
						}
						if(color.equals("R")){
							stack.push(null);
						}
					} else {
						if(gtNode.getChildCount()==2){
							TNode rightLCA = stack.pop();
							TNode leftLCA = stack.pop();
							// If gene trees are incomplete, we can have this case
							if (rightLCA == null && leftLCA == null) {
								stack.push(null);
								continue;
							}
							else if (rightLCA == null || leftLCA == null) {
								if(rightLCA != null){
									stack.push(rightLCA);
									LCAMap.put(gtNode.getID(), rightLCA.getID());
								}
								if(leftLCA != null){
									stack.push(leftLCA);
									LCAMap.put(gtNode.getID(), leftLCA.getID());
								}
	
								continue;
							}
							
							TNode lca = lcaLookup.getLCA(leftLCA, rightLCA);
							stack.push(lca);
							LCAMap.put(gtNode.getID(), lca.getID());
						}
						else{
							TNode[] children = new TNode[gtNode.getChildCount()];
							boolean allnull = true;
							for(int i =0;i < gtNode.getChildCount();i++){
								children[i] = stack.pop();
								if(children[i] != null)
									allnull = false;
							}
							if (allnull) {
								stack.push(null);
								continue;
							}
							else {
								Set<TNode> notnulls = new HashSet<TNode>();
								for(TNode child: children){
									if(child != null)
										notnulls.add(child);
								}
								TNode lca = lcaLookup.getLCA(notnulls);								
								stack.push(lca);
								LCAMap.put(gtNode.getID(), lca.getID());
								continue;
							}
							
						}

					}
				}
				return LCAMap;
			}
	
	}


	

