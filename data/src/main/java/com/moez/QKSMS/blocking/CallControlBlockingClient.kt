/*
 * Copyright (C) 2017 Moez Bhatti <moez.bhatti@gmail.com>
 *
 * This file is part of QKSMS.
 *
 * QKSMS is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * QKSMS is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with QKSMS.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.moez.QKSMS.blocking

import android.content.Context
import io.reactivex.Completable
import io.reactivex.Single
import timber.log.Timber
import javax.inject.Inject

/**
 * Call Control blocking client - STUBBED
 * The CallControl datashare library is no longer available on Maven Central.
 * This stub always returns isAvailable() = false so the app falls back to other blocking clients.
 */
class CallControlBlockingClient @Inject constructor(
    private val context: Context
) : BlockingClient {

    override fun isAvailable(): Boolean {
        Timber.d("CallControlBlockingClient: Library not available (deprecated)")
        return false
    }

    override fun getClientCapability() = BlockingClient.Capability.CANT_BLOCK

    override fun shouldBlock(address: String): Single<BlockingClient.Action> {
        return Single.just(BlockingClient.Action.DoNothing)
    }

    override fun isBlacklisted(address: String): Single<BlockingClient.Action> {
        return Single.just(BlockingClient.Action.DoNothing)
    }

    override fun block(addresses: List<String>): Completable {
        return Completable.complete()
    }

    override fun unblock(addresses: List<String>): Completable {
        return Completable.complete()
    }

    override fun openSettings() {
        // No-op - library not available
    }

}
