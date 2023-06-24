package com.makinu.app.scheduler.ui.main.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView.VERTICAL
import com.makinu.app.scheduler.R
import com.makinu.app.scheduler.data.Status
import com.makinu.app.scheduler.data.model.AppUiInfo
import com.makinu.app.scheduler.databinding.FragmentHomeBinding
import dagger.hilt.android.AndroidEntryPoint

/**
 * A simple [Fragment] subclass as the default destination in the navigation.
 */

@AndroidEntryPoint
class HomeFragment : Fragment() {

    private val viewModel: HomeViewModel by viewModels()
    private var _binding: FragmentHomeBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)

        prepareRecyclerView()
        return binding.root
    }

    private val items: ArrayList<AppUiInfo> = ArrayList()
    private lateinit var adapter: AppInfoAdapter

    private fun prepareRecyclerView() {
        adapter = AppInfoAdapter(items, object : AppInfoAdapter.OnClickListener {
            override fun clickOnView(position: Int, item: AppUiInfo) {

            }
        })

        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(requireActivity(), VERTICAL, false)
            adapter = this@HomeFragment.adapter
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.buttonFirst.setOnClickListener {
            findNavController().navigate(R.id.action_FirstFragment_to_SecondFragment)
        }

        viewModel.appUiInfos.observe(viewLifecycleOwner) { response ->
            response.getContentIfNotHandled()?.let {
                when (it.status) {
                    Status.LOADING -> {

                    }
                    Status.ERROR -> {

                    }
                    Status.SUCCESS -> {
                        items.clear()
                        it.data?.let { list ->
                            items.addAll(list)
                        }
                        updateView()
                    }
                }
            }
        }
//        viewModel.getAppStatus()
        context?.let { viewModel.getAllInstalledAppsUsingQuery(it) }
    }

    private fun updateView() {
        adapter.notifyDataSetChanged()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}