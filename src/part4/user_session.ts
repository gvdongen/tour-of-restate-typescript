/*
 * Copyright (c) 2023 - Restate Software, Inc., Restate GmbH
 *
 * This file is part of the Tour of Restate Typescript handler API,
 * which is released under the MIT license.
 *
 * You can find a copy of the license in the file LICENSE
 * in the root directory of this repository or package or at
 * https://github.com/restatedev/tour-of-restate-typescript
 */

import * as restate from "@restatedev/restate-sdk";
import { ticketServiceApi } from "./ticket_service";
import { checkoutApi } from "./checkout";

const addTicket = async (
  ctx: restate.RpcContext,
  userId: string,
  ticketId: string,
) => {
  // try to reserve ticket
  const reservation_success = await ctx.rpc(ticketServiceApi).reserve(ticketId);

  if (reservation_success) {
    // add ticket to user session tickets
    const tickets = (await ctx.get<string[]>("tickets")) ?? [];
    tickets.push(ticketId);
    ctx.set("tickets", tickets);

    // Schedule expiry timer
    ctx
      .sendDelayed(userSessionApi, 15 * 60 * 1000)
      .expireTicket(userId, ticketId);
  }

  return reservation_success;
};

const expireTicket = async (
  ctx: restate.RpcContext,
  userId: string,
  ticketId: string,
) => {
  const tickets = (await ctx.get<string[]>("tickets")) ?? [];

  const index = tickets.findIndex((id) => id === ticketId);

  // try removing ticket
  if (index != -1) {
    tickets.splice(index, 1);
    ctx.set("tickets", tickets);
    // unreserve if ticket was reserved before
    ctx.send(ticketServiceApi).unreserve(ticketId);
  }
};

const checkout = async (ctx: restate.RpcContext, userId: string) => {
  const tickets = (await ctx.get<string[]>("tickets")) ?? [];

  if (tickets.length > 0) {
    const checkout_success = await ctx
      .rpc(checkoutApi)
      .checkout({ userId: userId, tickets: tickets! });

    if (checkout_success) {
      // mark tickets as sold if checkout was successful
      for (const ticket_id of tickets) {
        ctx.send(ticketServiceApi).markAsSold(ticket_id);
      }
      ctx.clear("tickets");
    }

    return checkout_success;
  } else {
    // no tickets reserved
    return false;
  }
};

export const userSessionRouter = restate.keyedRouter({
  addTicket: addTicket,
  expireTicket: expireTicket,
  checkout: checkout,
});

export const userSessionApi: restate.ServiceApi<typeof userSessionRouter> = {
  path: "UserSession",
};
