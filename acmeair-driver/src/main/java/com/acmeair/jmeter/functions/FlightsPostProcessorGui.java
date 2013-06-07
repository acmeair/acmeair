/*******************************************************************************
* Copyright (c) 2013 IBM Corp.
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*******************************************************************************/
package com.acmeair.jmeter.functions;

import java.awt.BorderLayout;
import javax.swing.Box;
import org.apache.jmeter.processor.gui.AbstractPostProcessorGui;
import org.apache.jmeter.testelement.TestElement;

public class FlightsPostProcessorGui extends AbstractPostProcessorGui{

	private static final long serialVersionUID = 1L;

	public FlightsPostProcessorGui(){
		super();
		init();
	}

	@Override
	public TestElement createTestElement() {
		FlightsPostProcessor flightsProcessor = new FlightsPostProcessor();
         modifyTestElement(flightsProcessor);
         return flightsProcessor;
	}

	/**
	 * Sets the string seen in the jMeter GUI menu.
	 *
	 * @see org.apache.jmeter.gui.AbstractJMeterGuiComponent#getStaticLabel()
	 */
	@Override
	public String getStaticLabel() {
		return "AcmeAir Flights PostProcessor";
	}

	/**
	 * The getLabelResource method is not used because the
	 * getStaticLabel method that would normally call it is overridden.
	 * Still need to implement this abstract method.
	 *
	 * @see org.apache.jmeter.gui.JMeterGUIComponent#getLabelResource()
	 */
	@Override
	public String getLabelResource() {
		return "flightspostprocessor_title";
	}


	@Override
	public void modifyTestElement(TestElement te) {
	    super.configureTestElement(te);
	}

	private void init() {
		setLayout(new BorderLayout());
		setBorder(makeBorder());
		Box box = Box.createVerticalBox();
		box.add(makeTitlePanel());
		add(box, BorderLayout.NORTH);
	}

	public void configure(TestElement el) {
		super.configure(el);
	}
}
