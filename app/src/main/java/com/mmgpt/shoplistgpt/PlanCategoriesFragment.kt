package com.mmgpt.shoplistgpt

import android.os.Bundle
import android.view.GestureDetector
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.core.view.GestureDetectorCompat
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.mmgpt.shoplistgpt.adapter.ItemAdapter
import com.mmgpt.shoplistgpt.databinding.FragmentCategoryBinding
import com.mmgpt.shoplistgpt.ShoppingItem

class PlanCategoriesFragment : Fragment() {

    private val viewModel: ShoppingListViewModel by activityViewModels()
    private var _binding: FragmentCategoryBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentCategoryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        lateinit var adapter: ItemAdapter
        val dragHelper = createPlanDragHelper({ adapter }) { from, to ->
            viewModel.reorderCategory(from, to)
        }

        adapter = ItemAdapter(
            onAction = { item, action ->
                val index = adapter.currentList.indexOf(item) - 1
                when (action) {
                    ItemAdapter.Action.EDIT -> showTextEditDialog(item.name) { viewModel.editCategory(index, it) }
                    ItemAdapter.Action.DELETE -> viewModel.deleteCategory(index)
                    else -> {}
                }
            },
            mode = ItemAdapter.Mode.PLAN,
            dragStart = { vh -> dragHelper.startDrag(vh) },
            onAdd = { position, name ->
                viewModel.addCategory(position - 1, name)
            }
        )
        binding.recyclerView.adapter = adapter
        dragHelper.attachToRecyclerView(binding.recyclerView)

        binding.recyclerView.setRowDoubleTapListener { id ->
            val index = viewModel.categories.value?.indexOfFirst { it.id == id } ?: -1
            val viewPager = requireActivity().findViewById<ViewPager2>(R.id.viewPager)
            val page = index + 1 // page 0 is the category list
            if (index >= 0 && page < (viewPager.adapter?.itemCount ?: 0)) {
                viewPager.currentItem = page
            }
        }

        binding.btnClearAll.visibility = View.VISIBLE
        binding.btnClearAll.setOnClickListener {
            AlertDialog.Builder(requireContext())
                .setMessage(getString(R.string.confirm_clear_all))
                .setPositiveButton(android.R.string.ok) { _, _ ->
                    viewModel.clearAllCategories()
                }
                .setNegativeButton(android.R.string.cancel, null)
                .show()
        }

        viewModel.categories.observe(viewLifecycleOwner) { list ->
            binding.tvHeading.text = getString(R.string.categories_heading)
            binding.tvCount.text = ""
            adapter.submitList(
                list.map { ShoppingItem(it.id, it.heading, isHeading = true) }.withPlaceholders()
            )
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

