package zz.utils.ui.propertyeditors;

import java.awt.BorderLayout;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.util.Hashtable;

import javax.swing.JLabel;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import zz.utils.properties.IProperty;
import zz.utils.properties.IPropertyListener;
import zz.utils.properties.IRWProperty;

public class FloatPropertyEditor extends SimplePropertyEditor<Float>
implements IPropertyListener<Float>, ChangeListener, FocusListener
{
	public static final int LOGSLIDER_RANGE = 1000;
	private static final double K = LOGSLIDER_RANGE*LOGSLIDER_RANGE;
	private static final double LN_K = Math.log(K);
	
	private boolean itsChanging = false;
	
	private final JSlider itsSlider;
	private final JLabel itsValueLabel;
	
	public FloatPropertyEditor(IRWProperty<Float> aProperty)
	{
		super(aProperty);
		itsSlider = new JSlider();
		itsValueLabel = new JLabel();
		
		itsSlider.setMinimum(0);
		itsSlider.setMaximum(LOGSLIDER_RANGE);
		setValue0(getProperty().get());
		itsSlider.addChangeListener(this);
		itsSlider.addFocusListener(this);
		
		Hashtable<Integer, JLabel> theLabels = new Hashtable<Integer, JLabel>();
		for(int p=0;p<LOGSLIDER_RANGE;p+=LOGSLIDER_RANGE/5)
		{
			theLabels.put(p, new JLabel(""+getValue0(p)));
		}
		itsSlider.setLabelTable(theLabels);
		
		setLayout(new BorderLayout());
		add(itsSlider, BorderLayout.CENTER);
		add(itsValueLabel, BorderLayout.SOUTH);
	}
	
	private void setValue0(Float aValue)
	{
		float v = aValue != null ? aValue : 1f;
		double p0 = (Math.log(v)/LN_K) + 0.5;
		int p = (int) (p0*LOGSLIDER_RANGE);
		itsChanging = true;
		itsSlider.setValue(p);
		itsValueLabel.setText(""+aValue);
		itsChanging = false;
	}
	
	private float getValue0()
	{
		int p = itsSlider.getValue();
		return getValue0(p);
	}
	
	private float getValue0(int p)
	{
		if (p == 0) return 0;
		double v = Math.pow(K, (1.0*p/LOGSLIDER_RANGE)-0.5);
		return (float) v;
	}
	
	@Override
	public void addNotify()
	{
		super.addNotify();
		getProperty().addHardListener(this);
	}
	
	@Override
	public void removeNotify()
	{
		super.removeNotify();
		getProperty().removeListener(this);
	}
	
	public void propertyChanged(IProperty<Float> aProperty, Float aOldValue, Float aNewValue)
	{
		setValue0(aNewValue);
	}
	
	public void propertyValueChanged(IProperty<Float> aProperty)
	{
	}
	
	public void focusGained(FocusEvent aE)
	{
	}
	
	public void focusLost(FocusEvent aE)
	{
		getProperty().set(getValue0());
	}
	
	public void stateChanged(ChangeEvent aE)
	{
		if (! itsChanging) getProperty().set(getValue0());
	}
}
