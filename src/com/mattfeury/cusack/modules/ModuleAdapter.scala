package com.mattfeury.cusack.modules

import com.mattfeury.cusack.Cusack

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnClickListener
import android.view.ViewGroup
import android.widget.ArrayAdapter

case class ModuleAdapter(context:Context, resourceId:Int, items:List[Module[Cusack]]) extends ArrayAdapter(context, resourceId, items.toArray) {
    override def getView(position:Int, convertView:View, parent:ViewGroup) : View = {
        var view = convertView
        val module = getItem(position)

        if (view == null) {
            def inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE).asInstanceOf[LayoutInflater]
            view = inflater.inflate(resourceId, null)
        }

        module.render(view)

        view.setOnClickListener(new OnClickListener() {
            override def onClick(view:View) = {
                module.selected()
            }
        })

        return view;
    }
}