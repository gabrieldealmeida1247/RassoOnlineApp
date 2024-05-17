package com.example.rassoonlineapp.Admin.fragments
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ViewSwitcher
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.rassoonlineapp.Admin.adapter.AdminAccessAdapter
import com.example.rassoonlineapp.Admin.adapter.AdminServiceAdapter
import com.example.rassoonlineapp.Admin.adapter.AdminUsersAdapter
import com.example.rassoonlineapp.R

class DashboardFragment : Fragment() {

    private lateinit var adminUserAdapter: AdminUsersAdapter
    private lateinit var adminAcessoAdapter: AdminAccessAdapter
    private lateinit var adminServiceAdapter: AdminServiceAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_dashboard, container, false)
        val viewSwitcher = view.findViewById<ViewSwitcher>(R.id.view_switcher_admin)

        val btnUsuarios = view.findViewById<Button>(R.id.button_usuarios_admin)
        val btnAcessos = view.findViewById<Button>(R.id.button_acessos_admin)
        val btnServicos = view.findViewById<Button>(R.id.button_service_admin)

        adminAcessoAdapter = AdminAccessAdapter()
        adminServiceAdapter = AdminServiceAdapter()
        adminUserAdapter = AdminUsersAdapter()

        btnUsuarios.setOnClickListener {
            viewSwitcher.setDisplayedChild(0)
            showUsuarios()
        }

        btnAcessos.setOnClickListener {
            viewSwitcher.setDisplayedChild(1)
            showAcessos()
        }

        btnServicos.setOnClickListener {
            viewSwitcher.setDisplayedChild(2)
            showServicos()
        }



        return view
    }

    private fun showUsuarios() {
        view?.findViewById<RecyclerView>(R.id.recycler_view_usuarios_admin)?.apply {
            adapter = adminUserAdapter
            layoutManager = LinearLayoutManager(requireContext())
        }
        view?.findViewById<View>(R.id.recycler_view_usuarios_admin)?.visibility = View.VISIBLE
        view?.findViewById<View>(R.id.recycler_view_acesso_admin)?.visibility = View.GONE
        view?.findViewById<View>(R.id.recycler_view_servicos_admin)?.visibility = View.GONE
    }

    private fun showAcessos() {
        view?.findViewById<RecyclerView>(R.id.recycler_view_acesso_admin)?.apply {
            adapter = adminAcessoAdapter
            layoutManager = LinearLayoutManager(requireContext())
        }
        view?.findViewById<View>(R.id.recycler_view_usuarios_admin)?.visibility = View.GONE
        view?.findViewById<View>(R.id.recycler_view_acesso_admin)?.visibility = View.VISIBLE
        view?.findViewById<View>(R.id.recycler_view_servicos_admin)?.visibility = View.GONE
    }

    private fun showServicos() {
        view?.findViewById<RecyclerView>(R.id.recycler_view_servicos_admin)?.apply {
            adapter = adminServiceAdapter
            layoutManager = LinearLayoutManager(requireContext())
        }
        view?.findViewById<View>(R.id.recycler_view_usuarios_admin)?.visibility = View.GONE
        view?.findViewById<View>(R.id.recycler_view_acesso_admin)?.visibility = View.GONE
        view?.findViewById<View>(R.id.recycler_view_servicos_admin)?.visibility = View.VISIBLE
    }

}

