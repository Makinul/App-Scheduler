package com.makinu.app.scheduler.ui.main.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.makinu.app.scheduler.R
import com.makinu.app.scheduler.data.Status
import com.makinu.app.scheduler.data.model.AppInfo
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
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.buttonFirst.setOnClickListener {
            findNavController().navigate(R.id.action_FirstFragment_to_SecondFragment)
        }

        viewModel.appStatus.observe(viewLifecycleOwner) { response ->
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
        viewModel.getAppStatus()
        context?.let { viewModel.getQueryApps(it) }
    }

    private fun updateView() {

    }

    private val items: ArrayList<AppInfo> = ArrayList()

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}