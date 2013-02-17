package com.rsa.releng.simple_war.web.views;

import net.contextfw.web.application.component.Component;
import net.contextfw.web.application.component.Element;
import net.contextfw.web.application.lifecycle.PageScoped;
import net.contextfw.web.application.lifecycle.View;
import net.contextfw.web.application.lifecycle.ViewComponent;
import net.contextfw.web.application.lifecycle.ViewContext;

@View
@PageScoped
public class RootView extends Component implements ViewComponent {

    @Element
    private Component child;
    
    @Override
    public void initialize(ViewContext context) {
        if (context.getChildClass() != null) {
            child = context.initChild();
            registerChild(child);
        }
    }
}