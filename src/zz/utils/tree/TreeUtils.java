/*
 * Created on Jan 7, 2005
 */
package zz.utils.tree;

import zz.utils.Utils;

/**
 * Utilities for working with trees.
 * @author gpothier
 */
public class TreeUtils
{
	/**
	 * Searches the tree for a node that has the specified value.
	 */
	public static <N, V> N findNode(ITree<N, V> aTree, V aValue)
	{
		return findNode(aTree, aTree.getRoot(), aValue);
	}
	
	/**
	 * Searches a tree node for a children (or itself) with the specified value.
	 */
	public static <N, V> N findNode(ITree<N, V> aTree, N aNode, V aValue)
	{
		if (Utils.equalOrBothNull(aTree.getValue(aNode), aValue)) return aNode;
		
		for (N theChild : aTree.getChildren(aNode))
		{
			N theResult = findNode(aTree, theChild, aValue);
			if (theResult != null) return theResult;
		}
		
		return null;
	}
	
	/**
	 * Depths first visit of a tree.
	 */
	public static <N, V> void visit (ITree<N, V> aTree, ITreeVisitor<N, V> aVisitor)
	{
		visit (aTree, aTree.getRoot(), aVisitor);
	}

	/**
	 *  Depths-first visit of a tree starting at the specified node.
	 */
	public static <N, V> void visit (ITree<N, V> aTree, N aNode, ITreeVisitor<N, V> aVisitor)
	{
		aVisitor.visit(aNode, aTree.getValue(aNode));
		for (N theChild : aTree.getChildren(aNode))
		{
			visit(aTree, theChild, aVisitor);
		}
	}
}
