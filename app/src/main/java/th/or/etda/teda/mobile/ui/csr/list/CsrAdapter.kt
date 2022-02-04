/*
 * Copyright 2018 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package th.or.etda.teda.mobile.ui.csr.list

import android.app.Activity
import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.navigation.findNavController
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import th.or.etda.teda.mobile.data.csr.Csr
import th.or.etda.teda.mobile.databinding.ListItemCsrBinding
import th.or.etda.teda.mobile.ui.csr.directory.DirectoryCsrActivity


class CsrAdapter(requireCompatActivity: Activity) :
    ListAdapter<Csr, RecyclerView.ViewHolder>(CsrDiffCallback()) {

    var context = requireCompatActivity

    interface OnEventListener {
        fun onEvent(type: Int,item: Csr)
    }

    private var mOnEventListener: OnEventListener? = null

    fun setOnEventListener(listener: OnEventListener) {
        mOnEventListener = listener
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return ViewHolder(
            ListItemCsrBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )

    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val directory = getItem(position)
        (holder as ViewHolder).bind(directory, context, mOnEventListener)
    }

    class ViewHolder(private val binding: ListItemCsrBinding) :
        RecyclerView.ViewHolder(binding.root) {
        init {
        }


        fun bind(item: Csr, context: Activity, mOnEventListener: OnEventListener?) {
//            Log.i("aaa",item.certName)
            binding.apply {
//                certTitle.setText(item.certName)
                csr = item
                executePendingBindings()
                if (item.csrKey?.isEmpty() == true) {
                    csrStatus.text = "Waiting"
                    csrStatus.setTextColor(Color.GRAY)
                } else {
                    csrStatus.text = "Complete"
                    csrStatus.setTextColor(Color.GREEN)
                }

                if (item.chains?.isEmpty() == true) {
                    csrChainsStatus.text = "Waiting"
                    csrChainsStatus.setTextColor(Color.GRAY)
                } else {
                    csrChainsStatus.text = "Complete"
                    csrChainsStatus.setTextColor(Color.GREEN)
                }

                btnCsr.setOnClickListener {

//                   var action =  CsrListFragmentDirections.nextActionCer("cer")
//                    it.findNavController().navigate(action)

//                    val intent = Intent(context, DirectoryCsrActivity::class.java)
//                    context.startActivityForResult(intent,PICK_FILE)
//                    val intent = Intent(context, DirectoryCsrActivity::class.java)
//                    intent.putExtra("isCer", true)
//                    intent.putExtra("type", "p7b")
//                    context.startActivityForResult(intent, CsrListFragment.PICK_FILE)

                    mOnEventListener?.onEvent(0,item)

                }
                btnChains.setOnClickListener {
//                    var action =  CsrListFragmentDirections.nextActionCer("p7b")
//                    it.findNavController().navigate(action)
//                    val intent = Intent(context, DirectoryCsrActivity::class.java)
//                    intent.putExtra("type", "cer")
//                    context.startActivityForResult(intent, CsrListFragment.PICK_FILE)
                    mOnEventListener?.onEvent(1,item)
                }
                root.setOnClickListener {
                    mOnEventListener?.onEvent(2,item)
                }
                root.setOnLongClickListener {
                    mOnEventListener?.onEvent(3,item)
                    return@setOnLongClickListener true
                }
            }
        }
    }


}

private class CsrDiffCallback : DiffUtil.ItemCallback<Csr>() {

    override fun areItemsTheSame(oldItem: Csr, newItem: Csr): Boolean {
        return oldItem.csrName == newItem.csrName
    }

    override fun areContentsTheSame(oldItem: Csr, newItem: Csr): Boolean {
        return oldItem == newItem
    }
}
