package com.subgraph.sgmail.ui;

import org.eclipse.jface.action.ControlContribution;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

public class SearchBarContribution extends ControlContribution {

  private final SearchBar searchBar;
  
  public SearchBarContribution(SearchBar searchBar) {
    super("search");
    this.searchBar = searchBar;
  }

  @Override
  protected Control createControl(Composite parent) {
    return searchBar.createControl(parent);
  }
  
  @Override
  protected int computeWidth(Control control) {
    return 200;
  }

}
