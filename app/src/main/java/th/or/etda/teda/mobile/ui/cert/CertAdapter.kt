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

package th.or.etda.teda.mobile.ui.cert

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import th.or.etda.teda.mobile.data.Certificate
import th.or.etda.teda.mobile.databinding.ListItemCertBinding


class CertAdapter : ListAdapter<Certificate, RecyclerView.ViewHolder>(CertDiffCallback()) {


    interface OnEventListener {
        fun onEvent(date: Certificate) // or void onEvent(); as per your need
    }

    private var mOnEventListener: OnEventListener? = null

    fun setOnEventListener(listener: OnEventListener?) {
        mOnEventListener = listener
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return CertViewHolder(
            ListItemCertBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )

    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val cert = getItem(position)
        (holder as CertViewHolder).bind(cert)
    }

    class CertViewHolder(private val binding: ListItemCertBinding) : RecyclerView.ViewHolder(binding.root) {
        init {
//            binding.setClickListener {
//                binding.cert?.let { plant ->
////                    navigateToPlant(plant, it)
//
//                }
//            }
//            binding.cardView.setOnClickListener {
//
//            }
        }

        private fun navigateToPlant(
            cert: Certificate,
            view: View
        ) {
//            val direction =
//                HomeViewPagerFragmentDirections.actionViewPagerFragmentToPlantDetailFragment(
//                    plant.plantId
//                )
//            view.findNavController().navigate(direction)
        }

        fun bind(item: Certificate) {
//            Log.i("aaa",item.certName)
            binding.apply {
//                certTitle.setText(item.certName)
                cert = item
                executePendingBindings()

            }
        }
    }




}

private class CertDiffCallback : DiffUtil.ItemCallback<Certificate>() {

    override fun areItemsTheSame(oldItem: Certificate, newItem: Certificate): Boolean {
        return oldItem.certName == newItem.certName
    }

    override fun areContentsTheSame(oldItem: Certificate, newItem: Certificate): Boolean {
        return oldItem == newItem
    }
}
