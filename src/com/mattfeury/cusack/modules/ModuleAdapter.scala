package com.mattfeury.cusack.modules

import com.mattfeury.cusack.Cusack

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnClickListener
import android.view.ViewGroup
import android.widget.ArrayAdapter

// ResourceID isn't really honored here...
case class ModuleAdapter(context:Context, resourceId:Int, items:List[Module[Cusack]]) extends ArrayAdapter(context, resourceId, items.toArray) {
    override def getView(position:Int, convertView:View, parent:ViewGroup) : View = {
        val module = getItem(position)

        // We don't allow the adapter to cache views here since each module may use a different layout.
        // This may cause performance impacts but since we shouldn't have too many modules, we should be alright.
        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE).asInstanceOf[LayoutInflater]
        val view = inflater.inflate(module.templateId, null)

        module.render(view)

        view.setOnClickListener(new OnClickListener() {
            override def onClick(view:View) = {
                module.selected()
            }
        })

        return view;
    }
}