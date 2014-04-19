package com.mattfeury.cusack.modules

import com.mattfeury.cusack.Cusack
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnClickListener
import android.view.ViewGroup
import android.widget.ArrayAdapter
import com.mattfeury.cusack.modules.Expandable
import com.mattfeury.cusack.R
import com.mattfeury.cusack.CusackReceiver

// ResourceID isn't really honored here...
case class ModuleAdapter(context:Context, resourceId:Int, items:List[Module[Cusack]]) extends ArrayAdapter(context, resourceId, items.toArray) {
    override def getView(position:Int, convertView:View, parent:ViewGroup) : View = {
        val module = getItem(position)

        // We don't allow the adapter to cache views here since each module may use a different layout.
        // This may cause performance impacts but since we shouldn't have too many modules, we should be alright.
        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE).asInstanceOf[LayoutInflater]
        val view = inflater.inflate(module.templateId, null)

        module.render(view)

        // this should get moved to the module
        module match {
            case expandable:Expandable[Cusack] =>
                view.findViewById(R.id.moduleImage).setOnClickListener(ScalaOnClick(v => expandable.onSelect()))
                view.findViewById(R.id.moduleText).setOnClickListener(ScalaOnClick(v => expandable.toggle()))

            case _ =>
                view.setOnClickListener(ScalaOnClick(v => module.onSelect))
        }

        return view;
    }

    case class ScalaOnClick(func:View=>Unit) extends OnClickListener() {
        override def onClick(view:View) = func(view)
    }
}