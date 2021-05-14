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

package th.or.etda.teda.mobile.ui.importkey.directory

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import th.or.etda.teda.mobile.data.Certificate
import th.or.etda.teda.mobile.databinding.ListItemDirectoryBinding


class DirectoryAdapter : ListAdapter<Directory, RecyclerView.ViewHolder>(CertDiffCallback()) {




    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return ViewHolder(
            ListItemDirectoryBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )

    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val directory = getItem(position)
        (holder as ViewHolder).bind(directory)
    }

    class ViewHolder(private val binding: ListItemDirectoryBinding) : RecyclerView.ViewHolder(binding.root) {
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

        fun bind(item: Directory) {
//            Log.i("aaa",item.certName)
            binding.apply {
//                certTitle.setText(item.certName)
                directory = item
                executePendingBindings()

            }
        }
    }




}

private class CertDiffCallback : DiffUtil.ItemCallback<Directory>() {

    override fun areItemsTheSame(oldItem: Directory, newItem: Directory): Boolean {
        return oldItem.name == newItem.name
    }

    override fun areContentsTheSame(oldItem: Directory, newItem: Directory): Boolean {
        return oldItem == newItem
    }
}
